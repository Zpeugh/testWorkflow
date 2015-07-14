package flow

import groovy.xml.MarkupBuilder

/**
 * Created by zpeugh on 5/22/15.
 */
class Event {

	String nextEvent
	String plans
	String eventClass
	int version
	int eventID
	String projectLevel
	String eventName
	String locationType
	String nextEventLabel
	ArrayList<Integer> possibleNextEventIDs = new ArrayList<>()
	ArrayList<Event> possibleNextEvents = new ArrayList<>()
	ArrayList<Form> formTemplates = new ArrayList<>()
	ArrayList<Action> startActions = new ArrayList<>()
	ArrayList<Action> plannedActions = new ArrayList<>()
	ArrayList<Action> finishActions = new ArrayList<>()
	Condition condition = new Condition()

	public String toString(){

		String possibleEventString = ""
		if (!this.possibleNextEvents.is(null)){
			for (int i = 0 ; i < this.possibleNextEvents.size(); i++) {
				possibleEventString += this.possibleNextEvents[i].eventName.toString() + ", "
			}
		}

		return "EVENT: ${this.eventName}\nType: ${this.eventClass}\nCondition: ${this.condition}\nLevel:" +
			"${this.projectLevel}\nID: ${this.eventID}\nActions:\n\t\tOn Start- ${this.startActions.toString()}\n" +
			"\t\tOn Finish- ${this.finishActions.toString()}\n" +
			"\t\tPlanned- ${this.plannedActions.toString()}\n" +
			"Form: ${this.formTemplates.toString()}" + "\nNext possible events: " + possibleEventString
	}


	public printEventPage(File outputFile){

	
		def writer = new FileWriter(outputFile)
		def markup = new MarkupBuilder(writer)
		def thisCondition
		def posEvents = new ArrayList()
		def sActions = new ArrayList()
		def pActions = new ArrayList()
		def fActions = new ArrayList()
		def forms = new ArrayList()
		def conditionClass = "display"


		if (this.eventClass == "Conditional"){
			thisCondition = this.condition.conditionName
		} else {
			thisCondition = "Non-Conditional Event"
			conditionClass = "hidden"
		}
		if (this.startActions.size() != 0){
			this.startActions.each { sActions << '<a href=\"' + '../actionHtmls/' + "${it.toString()}" + '.html\">' + "${it.toString()}" + '</a>' }
			sActions = sActions.toString()
			sActions = sActions.substring(1, sActions.length()- 1) //remove brackets
		} else {
			sActions = "None for this event"
		}
		if (this.plannedActions.size() != 0){
			this.plannedActions.each { pActions << '<a href=\"' + '../actionHtmls/' + "${it.toString()}" + '.html\">' + "${it.toString()}" + '</a>' }
			pActions = pActions.toString()
			pActions = pActions.substring(1, pActions.length()- 1)
		} else {
			pActions = "None for this event"
		}
		if (this.finishActions.size() != 0){
			this.finishActions.each { fActions << '<a href=\"' + '../actionHtmls/' + "${it.toString()}" + '.html\">' + "${it.toString()}" + '</a>' }
			fActions = fActions.toString()
			fActions = fActions.substring(1, fActions.length()- 1)
		} else {
			fActions = "None for this event"
		}
		if (this.formTemplates.size() != 0){
			this.formTemplates.each { forms << '<a href=\"' + '../formHtmls/' + "${it.toString()}" + '.html\">' + "${it.toString()}" + '</a>' }
			forms = forms.toString()
			forms = forms.substring(1, forms.length() - 1)
		}
		else {
			forms = "No forms for this event"
		}
		if (this.possibleNextEvents.size() != 0){
			this.possibleNextEvents.each { posEvents << '<a href=\"' + "${it.eventName.toString()}" + '.html\">' + "${it.eventName.toString()}" + '</a>' }
			posEvents = posEvents.toString()
			posEvents = posEvents.substring(1, posEvents.length() - 1)
		} else{
			posEvents = "None"
		}


		markup.html{
			link(href : "../../../src/Resources/events.css", rel : 'stylesheet', type : "text/css"){}
			center {
				h1(class: "header") { mkp.yield "Event: ${this.eventName}" }
			}
			div(class: "event") {
				p { mkp.yield("Type: ${this.eventClass} ") }
				p { mkp.yield("Level: ${this.projectLevel}") }
				p (class : "${conditionClass}" ){ mkp.yield("Condition: ${thisCondition}") }
				p {mkp.yieldUnescaped("Next Possible Events: ${posEvents}")}
			}
			div(class: "actions"){
				hr(class: "break") {}
				h2(class : "header"){mkp.yield("Actions")}
				ul{
					li{mkp.yieldUnescaped("On start: " + sActions)}
					li{mkp.yieldUnescaped("Planned: " + pActions)}
					li{mkp.yieldUnescaped("On finish: " + fActions)}
				}
			}
			div(class: "forms") {
				hr (class: "break") {}
				h2(class : "header") { mkp.yield("Forms:") }
				p{ mkp.yieldUnescaped(forms) }			}
		}
	}



	public printEventInfoPage(File outputFile){

			def thisCondition
			String posEvents = ""
			String sActions = ""
			String pActions = ""
			String fActions = ""
			String forms = ""

			if (this.eventClass == "Conditional"){
				thisCondition = this.condition.conditionName
			} else {
				thisCondition = "Non-Conditional Event"
			}
			if (this.startActions.size() != 0){
				this.startActions.each { sActions += "~${it.toString()}" }
				sActions = sActions.substring(1) //remove initial ~
			} else {
				sActions = "None for this event"
			}
			if (this.plannedActions.size() != 0){
				this.plannedActions.each { pActions += "~${it.toString()}" }
				pActions = pActions.substring(1)
			} else {
				pActions = "None for this event"
			}
			if (this.finishActions.size() != 0){
				this.finishActions.each { fActions += "~${it.toString()}" }
				fActions = fActions.substring(1)
			} else {
				fActions = "None for this event"
			}
			if (this.formTemplates.size() != 0){
				this.formTemplates.each { forms += "~${it.toString()}" }
				forms = forms.substring(1)
			}
			else {
				forms = "No forms for this event"
			}
			if (this.possibleNextEvents.size() != 0){
				this.possibleNextEvents.each { posEvents += "~${it.eventName.toString()}" }
				posEvents = posEvents.substring(1)
			} else{
				posEvents = "None"
			}

			outputFile.write "Event: ${this.eventName.toUpperCase()}\n"
			outputFile.append "Type: ${this.eventClass}\n"
			outputFile.append "Level: ${this.projectLevel}\n"
			outputFile.append "Condition: ${thisCondition}\n"
			outputFile.append "Next Possible Events: ${posEvents}\n"
			outputFile.append "On start: ${sActions}\n"
			outputFile.append "Planned: ${pActions}\n"
			outputFile.append "On finish: ${fActions}\n"
			outputFile.append forms

		}

}
