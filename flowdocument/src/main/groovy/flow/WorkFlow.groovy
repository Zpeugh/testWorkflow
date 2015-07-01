package flow


import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import groovy.util.AntBuilder
//import static groovyx.gpars

import static groovy.json.JsonParserType.INDEX_OVERLAY

/**
 * Created by zpeugh on 5/22/15.
 */
public class WorkFlow {

	String flowName
	int companyID
	Form coreForm
	def events = new HashMap()
	def formNames = new ArrayList()
	def actionNames = new ArrayList()
	def conditionNames = new ArrayList()

	/*
	 *	Constructor
	 */
	public WorkFlow(String fileName){


		//Define the JSON file parser

		def jsonParser = new JsonSlurper().setType(INDEX_OVERLAY)


		//Store the parsed contents of the file in jsonObject

		File jsonFile = new File('Resources/' + fileName)
		def jsonObject = jsonParser.parse(jsonFile)


		//Define new workflow data representation

		//set name of flow from JSON file
		this.flowName = jsonObject.name

		//set company ID number from the JSON file
		this.companyID = jsonObject.companyId

		//creates an action map with key values as the action ID's and values as the flow.Action structures
		//SHOULD MAKE ACTIONMAP A PRIVATE STATIC VARIABLE SO IT ISN'T PASSED MULTIPLE TIMES
		def allActions = jsonObject.actions
		def actionMap = new HashMap<Integer, Action>()

		allActions.each {
			actionMap.put( [setActionData(it).actionID , setActionData(it)] )
		}

		actionMap.each {
			this.actionNames << it.getValue().actionName
		}

		//creates a condition map with condition ID's as keys and the conditions themselves as values
		def allConditions = jsonObject.conditions
		def conditionMap = new HashMap<Integer, Condition>()

		allConditions.each {
			conditionMap.put( [setConditionData(it).conditionID, setConditionData(it)] )
		}

		conditionMap.each {
			this.conditionNames << it.getValue().conditionName
		}

		//creates a form map with form ID's as keys and the forms as the values
		def allForms = jsonObject.formTemplates
		def formMap = new HashMap<Integer, Form>()

		allForms.each{
			formMap.put( [setFormData(it).formID , setFormData(it)] )
		}

		formMap.each{
			this.formNames << it.getValue().formName
		}

		//Sets the core Form of the workflow by retrieving it from the Form map created earlier
		this.coreForm = formMap.get(jsonObject.coreForm)

		//instantiate the Events map inside the workflow data structure and
		//store the json files events in allEvents
		this.events = new HashMap()
		def allEvents = jsonObject.events

		//enter all data for each event in the work flow
		allEvents.each {
			Event tempEvent = setEventData(it, actionMap, conditionMap, formMap)
			this.events.put(tempEvent.eventID, tempEvent)
		}

		this.events.values().each { event ->
			def possibleEventIDArray = event.possibleNextEventIDs
			event.possibleNextEventIDs.each { ID ->
				event.possibleNextEvents.add(this.events.get(ID))
			}
		}
	}


	private static Action setActionData(def singleAction) {

		Action node = new Action()

		node.actionID = singleAction.id
		node.actionLabel = singleAction.label
		node.actionName = singleAction.name
		node.actionParameters = singleAction.parameters
		node.actionVersion = singleAction.version

		return node

	}


//	Takes the data from the given condition object and places it in the proper attribute locations in a Condition.
//	Returns that condition

	private static Condition setConditionData(def singleCondition) {
		Condition node = new Condition()

		node.conditionID = singleCondition.id
		node.conditionName = singleCondition.name
		node.conditionLabel = singleCondition.label
		node.conditionParameters = singleCondition.parameters
		node.conditionVersion = singleCondition.version

		return node
	}

//	Takes the data from the given form template object and places it in the proper attribute locations in a Form.
//	Returns that flow.Form

	private static Form setFormData(def singleForm){
		Form node = new Form()

		node.formID = singleForm.id
		node.formClass = singleForm.get('class')
		node.fieldTemplates = singleForm.fieldTemplates
		node.formName = singleForm.name

		return node
	}

//	Given a JSON event from the workflow, fill in the appropriate data sections in
//	a single "Event" node.
//
	private static Event setEventData(def singleEvent, HashMap<Integer, Action> actionMap, HashMap<Integer, Action> conditionMap, HashMap<Integer, Action> formMap){

		Event node = new Event() //create a new flow.Event to put into the work flow data structure

		//set condition if the event is conditional
		if (singleEvent.get('class').toString() == "ConditionalEvent"){
			node.condition = conditionMap.get(singleEvent.condition)
		}

		//set non optional flow.Event attributes
		node.eventClass = singleEvent.get('class') - 'Event'
		node.eventID = singleEvent.id
		node.eventName = singleEvent.name
		node.projectLevel = singleEvent.projectLevel
		node.version = singleEvent.version
		node.locationType = singleEvent.locationType
		node.formTemplates = singleEvent.formTemplates
		node.nextEvent = singleEvent.nextEvent
		node.nextEventLabel = singleEvent.nextEventLabel

		//Set the possible next events
		singleEvent.possibleNextEvents.each { node.possibleNextEventIDs << it.get("id") }

		//fill in the start/planned/finish actions for the event
		singleEvent.onStartActions.each { node.startActions << actionMap.get(it) }
		singleEvent.onFinishActions.each { node.finishActions << actionMap.get(it) }
		singleEvent.plans.each { plansObject ->
				if (actionMap.containsKey(plansObject.plannedAction)){
					node.plannedActions << actionMap[plansObject.plannedAction]
				} else {
					node.plannedActions = null
				}
			}

		//fill in the formTemplates
		singleEvent.formTemplates.each { node.formTemplates << formMap.get(it)}

		return node
	}



	public void printEventIndexPage(File outputFile){

		def writer = new FileWriter(outputFile)
		def markup = new MarkupBuilder(writer)
		String eventList = new File('Resources/Events/order.txt').text;
		def events = new ArrayList()
		def order = eventList.tokenize('~');

		for (int i = 0; i < order.size(); i++){
			events << ('\n\t\t<li><a href=\"' + "${order[i]}" +
				'.html\">' + "${order[i]}" + '</a></li>')
		}
		events = events.join("").toString()
		markup.html{
			link(href : "events.css", rel : 'stylesheet', type : "text/css"){}
			h1{
				mkp.yield("Event Index Page")
			}
			ul (class : "eventIndex"){
				mkp.yieldUnescaped(events.toString())
			}
		}
	}


	public static void main(args){

		def companyName = args[0]
		def fileName = args[1]
		WorkFlow newFlow = new WorkFlow(fileName)
		SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
		File flowInfo = new File('Resources/flowInfo.txt')
		new File('Resources/Events/texts').mkdir()

		flowInfo.write(newFlow.flowName + '\n')
		flowInfo.append(companyName.replaceAll('~', ' '))

		def getForms = ('casperjs.bat --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ' + companyName + ' ' + sampleFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def formatFormHtml = ('casperjs generateformhtml.js ' +  sampleFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ' + companyName + ' ' + sampleFlow.flowName + ' ' + sampleFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def formatActionHtml = ('casperjs generateactionhtml.js ' + sampleFlow.actionArguments).execute()
		formatActionHtml.waitForProcessOutput(System.out, System.err)

		newFlow.events.each{k,v -> v.printEventPage(new File('Resources/Events/' + v.eventName + '.html'))}

		newFlow.printEventIndexPage(new File('Resources/Events/EventIndex.html') )

		newFlow.events.each{k,v ->
			File eventPage = new File('Resources/Events/texts/' + v.eventName + '.txt')
			v.printEventInfoPage(eventPage)
		}
	}
}
