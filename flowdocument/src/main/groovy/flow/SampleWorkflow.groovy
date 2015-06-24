package flow

class SampleWorkFlow{

	def workFlow
	def jsonFileName
	def allForms
	def allActions
	def formsMissed
	def actionsMissed
	def formArguments
	def actionArguments
	def flowName

	public SampleWorkFlow(def fileName){
		this.workFlow = new WorkFlow(fileName)
		this.allForms = this.workFlow.formNames
		this.allActions = this.workFlow.actionNames
		this.flowName = this.workFlow.flowName.replaceAll(' ', '~')
		def formArgs = [];
		this.allForms.each{ formArgs << it.replaceAll(' ', '~') }
		this.formArguments = formArgs.join(' ')

		def actionArgs = [];
		this.allActions.each{ actionArgs << it.replaceAll(' ', '~') }
		this.actionArguments = actionArgs.join(' ')
	}
}
