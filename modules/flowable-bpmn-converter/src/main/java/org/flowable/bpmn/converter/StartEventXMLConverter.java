/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.bpmn.converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.converter.child.BaseChildElementParser;
import org.flowable.bpmn.converter.child.VariableListenerEventDefinitionParser;
import org.flowable.bpmn.converter.util.BpmnXMLUtil;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.alfresco.AlfrescoStartEvent;

/**
 * @author Tijs Rademakers
 */
public class StartEventXMLConverter extends BaseBpmnXMLConverter {
    
    protected Map<String, BaseChildElementParser> childParserMap = new HashMap<>();
    
    protected static final List<ExtensionAttribute> defaultStartEventAttributes = Arrays.asList(
            new ExtensionAttribute(ATTRIBUTE_FORM_FORMKEY),
            new ExtensionAttribute(ATTRIBUTE_FORM_FIELD_VALIDATION),
            new ExtensionAttribute(ATTRIBUTE_EVENT_START_INITIATOR),
            new ExtensionAttribute(ATTRIBUTE_EVENT_START_INTERRUPTING),
            new ExtensionAttribute(ATTRIBUTE_SAME_DEPLOYMENT));
    
    public StartEventXMLConverter() {
        VariableListenerEventDefinitionParser variableListenerEventDefinitionParser = new VariableListenerEventDefinitionParser();
        childParserMap.put(variableListenerEventDefinitionParser.getElementName(), variableListenerEventDefinitionParser);
    }

    @Override
    public Class<? extends BaseElement> getBpmnElementType() {
        return StartEvent.class;
    }

    @Override
    protected String getXMLElementName() {
        return ELEMENT_EVENT_START;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {
        String formKey = BpmnXMLUtil.getAttributeValue(ATTRIBUTE_FORM_FORMKEY, xtr);
        StartEvent startEvent = null;
        if (StringUtils.isNotEmpty(formKey)) {
            if (model.getStartEventFormTypes() != null && model.getStartEventFormTypes().contains(formKey)) {
                startEvent = new AlfrescoStartEvent();
            }
        }
        if (startEvent == null) {
            startEvent = new StartEvent();
        }

        BpmnXMLUtil.addXMLLocation(startEvent, xtr);
        String elementId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
        startEvent.setId(elementId);
        
        startEvent.setInitiator(BpmnXMLUtil.getAttributeValue(ATTRIBUTE_EVENT_START_INITIATOR, xtr));
        boolean interrupting = true;
        String interruptingAttribute = xtr.getAttributeValue(null, ATTRIBUTE_EVENT_START_INTERRUPTING);
        if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(interruptingAttribute)) {
            interrupting = false;
        }

        startEvent.setInterrupting(interrupting);
        startEvent.setFormKey(formKey);
        String formValidation = BpmnXMLUtil.getAttributeValue(BpmnXMLConstants.ATTRIBUTE_FORM_FIELD_VALIDATION, xtr);
        startEvent.setValidateFormFields(formValidation);

        String sameDeploymentAttribute = BpmnXMLUtil.getAttributeValue(ATTRIBUTE_SAME_DEPLOYMENT, xtr);
        if (ATTRIBUTE_VALUE_FALSE.equalsIgnoreCase(sameDeploymentAttribute)) {
            startEvent.setSameDeployment(false);
        }
        
        BpmnXMLUtil.addCustomAttributes(xtr, startEvent, defaultElementAttributes, defaultActivityAttributes, defaultStartEventAttributes);

        parseChildElements(getXMLElementName(), startEvent, childParserMap, model, xtr);

        return startEvent;
    }

    @Override
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
        StartEvent startEvent = (StartEvent) element;
        writeQualifiedAttribute(ATTRIBUTE_EVENT_START_INITIATOR, startEvent.getInitiator(), xtw);
        writeQualifiedAttribute(ATTRIBUTE_FORM_FORMKEY, startEvent.getFormKey(), xtw);
        writeQualifiedAttribute(ATTRIBUTE_FORM_FIELD_VALIDATION, startEvent.getValidateFormFields(), xtw);

        if (!startEvent.isSameDeployment()) {
            // default value is true
            writeQualifiedAttribute(ATTRIBUTE_SAME_DEPLOYMENT, "false", xtw);
        }

        if (startEvent.getEventDefinitions() != null && startEvent.getEventDefinitions().size() > 0) {
            writeDefaultAttribute(ATTRIBUTE_EVENT_START_INTERRUPTING, String.valueOf(startEvent.isInterrupting()), xtw);
        }
    }

    @Override
    protected boolean writeExtensionChildElements(BaseElement element, boolean didWriteExtensionStartElement, XMLStreamWriter xtw) throws Exception {
        StartEvent startEvent = (StartEvent) element;
        didWriteExtensionStartElement = writeVariableListenerDefinition(startEvent, didWriteExtensionStartElement, xtw);
        didWriteExtensionStartElement = writeFormProperties(startEvent, didWriteExtensionStartElement, xtw);
        return didWriteExtensionStartElement;
    }

    @Override
    protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {
        StartEvent startEvent = (StartEvent) element;
        writeEventDefinitions(startEvent, startEvent.getEventDefinitions(), model, xtw);
    }
}
