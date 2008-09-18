/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import java.util.List;

import org.apache.commons.logging.*;
import org.apache.axis.utils.XMLUtils;

import gov.bnl.gums.admin.GUMSAPI;
import gov.bnl.gums.admin.GUMSAPIImpl;

import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.ctx.StatusType;
import org.opensaml.xacml.ctx.StatusCodeType;
import org.opensaml.xacml.ctx.SubjectType;
import org.opensaml.xacml.ctx.ResultType;
import org.opensaml.xacml.ctx.ResourceType;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.ctx.impl.DecisionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusCodeTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResultTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResponseTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImpl;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeTypeImplBuilder;
import org.opensaml.xacml.policy.AttributeAssignmentType;
import org.opensaml.xacml.policy.EffectType;
import org.opensaml.xacml.policy.ObligationsType;
import org.opensaml.xacml.policy.ObligationType;
import org.opensaml.xacml.policy.impl.AttributeAssignmentTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationsTypeImplBuilder;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.XMLObject;

import org.opensciencegrid.authz.xacml.service.XACMLMappingService;
import org.opensciencegrid.authz.xacml.common.XACMLConstants;
import org.opensciencegrid.authz.xacml.client.XACMLClient;

public class GUMSXACMLMappingServiceImpl implements XACMLMappingService {
	private static String RESOURCE_ID = XACMLConstants.RESOURCE_DNS_HOST_NAME_ID;
	private static String SUBJECT_ID = XACMLConstants.SUBJECT_X509_ID;
	private static String VOMS_FQAN = XACMLConstants.SUBJECT_VOMS_FQAN_ID;
	private static String USERNAME =  XACMLConstants.OBLIGATION_USERNAME;
	private static String ERROR = "http://oasis/names/tc/xacml/1.0/status/error";
	private static String OK = "http://oasis/names/tc/xacml/1.0/status/ok";
	private Log log = LogFactory.getLog(GUMSXACMLMappingServiceImpl.class);
	private static GUMSAPI gums = new GUMSAPIImpl();

	public XACMLAuthzDecisionStatementType mapCredentials(XACMLAuthzDecisionQueryType xacmlQuery) throws Exception {
		XMLObjectBuilderFactory builderFactory = org.opensaml.xml.Configuration.getBuilderFactory();

		log.debug("XACMLAuthzDecisionQueryType object received: "+XMLUtils.ElementToString(xacmlQuery.getDOM()));

		// Get information from request
		RequestType request = xacmlQuery.getRequest();
		String hostDn = getResourceAttributeValue(request, RESOURCE_ID);
		String userDn = getSubjectAttributeValue(request, SUBJECT_ID);
		String userFqan = getSubjectAttributeValue(request, VOMS_FQAN);
		
		if (hostDn==null || hostDn.length()==0) {
			log.debug("missing attribute: "+RESOURCE_ID);
			throw new Exception("missing attribute: "+RESOURCE_ID);
		}
		if (userDn==null || userDn.length()==0) {
			log.debug("missing attribute: "+SUBJECT_ID);
			throw new Exception("missing attribute: "+SUBJECT_ID);
		}
		if (userFqan==null || userFqan.length()==0) {
			log.debug("missing attribute: "+VOMS_FQAN);
			throw new Exception("missing attribute: "+VOMS_FQAN);
		}

		// Attribute Assignment, decision, and status code
		AttributeAssignmentType attributeAssignment = null;
		DecisionTypeImplBuilder decisionBuilder = (DecisionTypeImplBuilder)builderFactory.getBuilder(DecisionType.DEFAULT_ELEMENT_NAME);
		DecisionType decision = decisionBuilder.buildObject();
		StatusCodeTypeImplBuilder statusCodeBuilder = (StatusCodeTypeImplBuilder)builderFactory.getBuilder(StatusCodeType.DEFAULT_ELEMENT_NAME);
		StatusCodeType statusCode = statusCodeBuilder.buildObject();
		statusCode.setValue(OK);
		try {
			log.debug("Checking access on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "'");
			String account = gums.mapUser(hostDn, userDn, userFqan);
			if (account == null) {
				decision.setDecision(DecisionType.DECISION.Deny);
				
				log.debug("Denied access on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "'");
			}
			else {
				AttributeAssignmentTypeImplBuilder attributeAssignmentBuilder = (AttributeAssignmentTypeImplBuilder)builderFactory.getBuilder(AttributeAssignmentType.DEFAULT_ELEMENT_NAME);
				attributeAssignment = attributeAssignmentBuilder.buildObject();
				attributeAssignment.setAttributeId(XACMLConstants.ATTRIBUTE_USERNAME_ID);
				attributeAssignment.setDataType(XACMLConstants.STRING_DATATYPE);
				attributeAssignment.setValue(account);

				decision.setDecision(DecisionType.DECISION.Permit);
				
				log.debug("Credentials mapped on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "' to '" + account + "'");
			}
		} catch (RuntimeException e1) {
			statusCode.setValue(ERROR);
			log.debug(e1.getMessage());
			throw e1;
		}

		// Status
		StatusTypeImplBuilder statusBuilder = (StatusTypeImplBuilder)builderFactory.getBuilder(StatusType.DEFAULT_ELEMENT_NAME);
		StatusType status = statusBuilder.buildObject();
		status.setStatusCode(statusCode);

		// Obligation
		ObligationTypeImplBuilder obligationBuilder = (ObligationTypeImplBuilder)builderFactory.getBuilder(ObligationType.DEFAULT_ELEMENT_QNAME);
		ObligationType obligation = obligationBuilder.buildObject();
		obligation.setFulfillOn(EffectType.Permit);
		obligation.setObligationId(USERNAME);
		if (attributeAssignment != null)
			obligation.getAttributeAssignments().add(attributeAssignment);

		// Obligations
		ObligationsTypeImplBuilder obligationsBuilder = (ObligationsTypeImplBuilder)builderFactory.getBuilder(ObligationsType.DEFAULT_ELEMENT_QNAME);
		ObligationsType obligations = obligationsBuilder.buildObject();
		obligations.getObligations().add(obligation);

		// Result
		ResultTypeImplBuilder resultBuilder = (ResultTypeImplBuilder)builderFactory.getBuilder(ResultType.DEFAULT_ELEMENT_NAME);
		ResultType result = resultBuilder.buildObject();
		result.setStatus(status);
		result.setDecision(decision);
		result.setObligations(obligations);

		// Response      
		ResponseTypeImplBuilder responseBuilder = (ResponseTypeImplBuilder)builderFactory.getBuilder(ResponseType.DEFAULT_ELEMENT_NAME);
		ResponseType response = responseBuilder.buildObject();
		response.setResult(result);

		// Statement
		XACMLAuthzDecisionStatementTypeImplBuilder xacmlauthzBuilder = (XACMLAuthzDecisionStatementTypeImplBuilder)builderFactory.getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
		XACMLAuthzDecisionStatementType xacmlAuthzStatement = xacmlauthzBuilder.buildObject( Statement.DEFAULT_ELEMENT_NAME, XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);	
		//xacmlAuthzStatement.setRequest(request);
		xacmlAuthzStatement.setResponse(response);

		log.debug("XACMLAuthzDecisionStatementType object returned: "+XMLUtils.ElementToString(xacmlAuthzStatement.getDOM()));
		
		return xacmlAuthzStatement;
	}

	private String getSubjectAttributeValue(RequestType request, String attributeId) {
		List<SubjectType> subjectList = request.getSubjects();
		for(SubjectType subject : subjectList) {
			List<AttributeType> attributeList = subject.getAttributes();
			for(AttributeType attribute : attributeList) {
				String curAttributeId = attribute.getAttributeID();
				if (attributeId.equals(curAttributeId)) {
					List<XMLObject> attributeValueList = attribute.getAttributeValues();
					for(XMLObject attributeValue : attributeValueList) {
						return ((AttributeValueTypeImpl)attributeValue).getValue();
					}
				}
			}
		}  
		return null;
	}

	private String getResourceAttributeValue(RequestType request, String attributeId) {
		List<ResourceType> resourceList = request.getResources();
		for(ResourceType resource : resourceList) {
			List<AttributeType> attributeList = resource.getAttributes();
			for(AttributeType attribute : attributeList) {
				String curAttributeId = attribute.getAttributeID();
				if (attributeId.equals(curAttributeId)) {
					List<XMLObject> attributeValueList = attribute.getAttributeValues();
					for(XMLObject attributeValue : attributeValueList) {
						return ((AttributeValueTypeImpl)attributeValue).getValue();
					}
				}
			}
		}  
		return null;
	}
}
