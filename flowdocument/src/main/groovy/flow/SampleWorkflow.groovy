package flow

import java.net.URLEncoder

class SampleWorkFlow{

	def workFlow
	def jsonFileName
	def allForms
	def allActions
	def formArguments
	def actionArguments
	def flowName

	public SampleWorkFlow(def fileName){
		this.workFlow = new WorkFlow(fileName)
		this.allForms = this.workFlow.formNames
		this.allActions = this.workFlow.actionNames
		this.flowName = URLEncoder.encode( this.workFlow.flowName, 'UTF-8')

		def formArgs = [];
		this.allForms.each{ formArgs << URLEncoder.encode(it , 'UTF-8') }
		this.formArguments = formArgs

		def actionArgs = [];
		this.allActions.each{ actionArgs << URLEncoder.encode(it, 'UTF-8') }
		this.actionArguments = actionArgs
	}
}
