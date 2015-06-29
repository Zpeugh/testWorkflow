package flow

import spock.lang.*

class TestWorkFlow extends Specification{

	//demo.spidastudio flows
	// def fridayFlow = new SampleWorkFlow('Friday.json')
	// def testerFlow = new SampleWorkFlow('Tester.json')
	// def actionsGaloreFlow = new SampleWorkFlow('ActionsGalore.json')
	// def NCflow = new SampleWorkFlow('North-Carolina-Make-Ready-Process.json')
	// def NCTestflow = new SampleWorkFlow('NCTest (1).json')

	//spidasoftware flows
	def testFlow = 	new SampleWorkFlow('fullExample.json')
	def dlcFlow = new SampleWorkFlow('DLC.json')
	def jointUseFlow = new SampleWorkFlow('Joint-Use-Workflow.json')
	def overheadEJUflow = new SampleWorkFlow('Overhead-EJU.json')
	def ejuflow = new SampleWorkFlow('EJU.json')
	def overlashFlow = new SampleWorkFlow('Overlash--imported-4-9-13-11-11-07-PM-.json')
	def dgPermitFlow = new SampleWorkFlow('DGPermitOLD.json')
	def ncmrpFlow = new SampleWorkFlow('North-Carolina-Make-Ready-Process (1).json')
	def juPermittingFlow = new SampleWorkFlow('JU-Permitting.json')
	def jpaFlow = new SampleWorkFlow('JPA--1-.json')
	def rfaFlow = new SampleWorkFlow('RFA--10-.json')
	def peiFlow = new SampleWorkFlow('PEI--imported-4-30-13-10-38-46-AM-.json')
	def poleLoadFlow = new SampleWorkFlow('Pole-Load-Analysis.json')
	def oldJPAflow = new SampleWorkFlow('JPA-old.json')
	def juFlow = new SampleWorkFlow('JU--UPDATED.json')


	def '1: SPIDA: Test-Flow: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + testFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		testFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		testFlow.formsMissed = missedList

		then:
		testFlow.formsMissed.size() == 0

	}

	def '2: SPIDA: Test-Flow: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  testFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + testFlow.allForms
		println "Missed Forms: " + testFlow.formsMissed
		def formIntersection = testFlow.allForms - testFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def '3: SPIDA: Test-Flow: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA Test-Flow ' + testFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		testFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		testFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def '4: SPIDA: Test-Flow: Generated all actions'() {

		println "args: " + testFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + testFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + testFlow.allActions
		println "Missed actions: " + testFlow.actionsMissed
		def actionIntersection = testFlow.allActions - testFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}




	//Testing DLC: DLC Overhead
	def '5: DLC: DLC Overhead: Capture all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js DLC ' + dlcFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		dlcFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		dlcFlow.formsMissed = missedList

		then:
		dlcFlow.formsMissed.size() == 0

	}

	def '6: DLC: DLC Overhead: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  dlcFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + dlcFlow.allForms
		println "Missed Forms: " + dlcFlow.formsMissed
		def formIntersection = dlcFlow.allForms - dlcFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def '7: DLC: DLC Overhead: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js DLC DLC~Overhead ' + dlcFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		dlcFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		dlcFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def '8: DLC: DLC Overhead: Generated all actions'() {

		println "args: " + dlcFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + dlcFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + dlcFlow.allActions
		println "Missed actions: " + dlcFlow.actionsMissed
		def actionIntersection = dlcFlow.allActions - dlcFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing ACME~Power~Co.:Joint Use WorkFlow flow
	def '9: ACME~Power~Co.: Joint Use WorkFlow: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ACME~Power~Co. ' + jointUseFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		jointUseFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		jointUseFlow.formsMissed = missedList

		then:
		jointUseFlow.formsMissed.size() == 0

	}

	def '10: ACME~Power~Co.: Joint Use WorkFlow: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  jointUseFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + jointUseFlow.allForms
		println "Missed Forms: " + jointUseFlow.formsMissed
		def formIntersection = jointUseFlow.allForms - jointUseFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def '11: ACME~Power~Co.: Joint Use WorkFlow: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ACME~Power~Co. ' + jointUseFlow.flowName + ' ' + jointUseFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		jointUseFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		jointUseFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def '12: ACME~Power~Co.: Joint Use WorkFlow: Generated all actions'() {

		println "args: " + jointUseFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + jointUseFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + jointUseFlow.allActions
		println "Missed actions: " + jointUseFlow.actionsMissed
		def actionIntersection = jointUseFlow.allActions - jointUseFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}





	//Testing ACME~Power~Co.:Overhead EJU flow
	def 'ACME~Power~Co.: Overhead EJU: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ACME~Power~Co. ' + overheadEJUflow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		overheadEJUflow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		overheadEJUflow.formsMissed = missedList

		then:
		overheadEJUflow.formsMissed.size() == 0

	}

	def 'ACME~Power~Co.: Overhead EJU: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  overheadEJUflow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + overheadEJUflow.allForms
		println "Missed Forms: " + overheadEJUflow.formsMissed
		def formIntersection = overheadEJUflow.allForms - overheadEJUflow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'ACME~Power~Co.: Overhead EJU: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ACME~Power~Co. ' + overheadEJUflow.flowName + ' ' + overheadEJUflow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		overheadEJUflow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		overheadEJUflow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'ACME~Power~Co.: Overhead EJU: Generated all actions'() {

		println "args: " + overheadEJUflow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + overheadEJUflow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + overheadEJUflow.allActions
		println "Missed actions: " + overheadEJUflow.actionsMissed
		def actionIntersection = overheadEJUflow.allActions - overheadEJUflow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	//Testing American~Electric~Power:EJU flow
	def 'American~Electric~Power: EJU: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js American~Electric~Power ' + ejuflow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		ejuflow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		ejuflow.formsMissed = missedList

		then:
		ejuflow.formsMissed.size() == 0

	}

	def 'American~Electric~Power: EJU: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  ejuflow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + ejuflow.allForms
		println "Missed Forms: " + ejuflow.formsMissed
		def formIntersection = ejuflow.allForms - ejuflow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'American~Electric~Power: EJU: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js American~Electric~Power ' + ejuflow.flowName + ' ' + ejuflow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		ejuflow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		ejuflow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'American~Electric~Power: EJU: Generated all actions'() {

		println "args: " + ejuflow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + ejuflow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + ejuflow.allActions
		println "Missed actions: " + ejuflow.actionsMissed
		def actionIntersection = ejuflow.allActions - ejuflow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	//Testing American~Electric~Power:Overlash (imported~4/9/13~11:11:01~PM) flow
	def 'American~Electric~Power: Overlash (imported~4/9/13~11:11:01~PM): Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js American~Electric~Power ' + overlashFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		overlashFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		overlashFlow.formsMissed = missedList

		then:
		overlashFlow.formsMissed.size() == 0

	}

	def 'American~Electric~Power: Overlash (imported~4/9/13~11:11:01~PM): Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  overlashFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + overlashFlow.allForms
		println "Missed Forms: " + overlashFlow.formsMissed
		def formIntersection = overlashFlow.allForms - overlashFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'American~Electric~Power: Overlash (imported~4/9/13~11:11:01~PM): Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js American~Electric~Power ' + overlashFlow.flowName + ' ' + overlashFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		overlashFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		overlashFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'American~Electric~Power: Overlash (imported~4/9/13~11:11:01~PM): Generated all actions'() {

		println "args: " + overlashFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + overlashFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + overlashFlow.allActions
		println "Missed actions: " + overlashFlow.actionsMissed
		def actionIntersection = overlashFlow.allActions - overlashFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing DG:DGPermitOLD flow
	def 'DG: DGPermitOLD: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js DG ' + dgPermitFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		dgPermitFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		dgPermitFlow.formsMissed = missedList

		then:
		dgPermitFlow.formsMissed.size() == 0

	}

	def 'DG: DGPermitOLD: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  dgPermitFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + dgPermitFlow.allForms
		println "Missed Forms: " + dgPermitFlow.formsMissed
		def formIntersection = dgPermitFlow.allForms - dgPermitFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'DG: DGPermitOLD: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js DG ' + dgPermitFlow.flowName + ' ' + dgPermitFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		dgPermitFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		dgPermitFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'DG: DGPermitOLD: Generated all actions'() {

		println "args: " + dgPermitFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + dgPermitFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + dgPermitFlow.allActions
		println "Missed actions: " + dgPermitFlow.actionsMissed
		def actionIntersection = dgPermitFlow.allActions - dgPermitFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing DG2:North~Carolina~Make~Ready~Process flow
	def 'DG2: North~Carolina~Make~Ready~Process: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js DG2 ' + ncmrpFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		ncmrpFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		ncmrpFlow.formsMissed = missedList

		then:
		ncmrpFlow.formsMissed.size() == 0

	}

	def 'DG2: North~Carolina~Make~Ready~Process: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  ncmrpFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + ncmrpFlow.allForms
		println "Missed Forms: " + ncmrpFlow.formsMissed
		def formIntersection = ncmrpFlow.allForms - ncmrpFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'DG2: North~Carolina~Make~Ready~Process: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js DG2 ' + ncmrpFlow.flowName + ' ' + ncmrpFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		ncmrpFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		ncmrpFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'DG2: North~Carolina~Make~Ready~Process: Generated all actions'() {

		println "args: " + ncmrpFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + ncmrpFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + ncmrpFlow.allActions
		println "Missed actions: " + ncmrpFlow.actionsMissed
		def actionIntersection = ncmrpFlow.allActions - ncmrpFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing DLC:DLC~Overhead flow
	def 'Duke Energy: JU Permitting: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js DLC ' + juPermittingFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		juPermittingFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		juPermittingFlow.formsMissed = missedList

		then:
		juPermittingFlow.formsMissed.size() == 0

	}

	def 'Duke Energy: JU Permitting: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  juPermittingFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + juPermittingFlow.allForms
		println "Missed Forms: " + juPermittingFlow.formsMissed
		def formIntersection = juPermittingFlow.allForms - juPermittingFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'Duke Energy: JU Permitting: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js Duke~Energy ' + juPermittingFlow.flowName + ' ' + juPermittingFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		juPermittingFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		juPermittingFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'Duke Energy: JU Permitting: Generated all actions'() {

		println "args: " + juPermittingFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + juPermittingFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + juPermittingFlow.allActions
		println "Missed actions: " + juPermittingFlow.actionsMissed
		def actionIntersection = juPermittingFlow.allActions - juPermittingFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing JPO:JPA~(1) flow
	def 'JPO: JPA~(1): Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js JPO ' + jpaFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		jpaFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		jpaFlow.formsMissed = missedList

		then:
		jpaFlow.formsMissed.size() == 0

	}

	def 'JPO: JPA~(1): Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  jpaFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + jpaFlow.allForms
		println "Missed Forms: " + jpaFlow.formsMissed
		def formIntersection = jpaFlow.allForms - jpaFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'JPO: JPA~(1): Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js JPO ' + jpaFlow.flowName + ' ' + jpaFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		jpaFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		jpaFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'JPO: JPA~(1): Generated all actions'() {

		println "args: " + jpaFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + jpaFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + jpaFlow.allActions
		println "Missed actions: " + jpaFlow.actionsMissed
		def actionIntersection = jpaFlow.allActions - jpaFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	//Testing JPO:JPA~(1) flow
	def 'JPO: RFA (10): Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js JPO ' + rfaFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		rfaFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		rfaFlow.formsMissed = missedList

		then:
		rfaFlow.formsMissed.size() == 0

	}

	def 'JPO: RFA (10): Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  rfaFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + rfaFlow.allForms
		println "Missed Forms: " + rfaFlow.formsMissed
		def formIntersection = rfaFlow.allForms - rfaFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'JPO: RFA (10): Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js JPO ' + rfaFlow.flowName + ' ' + rfaFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		rfaFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		rfaFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'JPO: RFA (10): Generated all actions'() {

		println "args: " + rfaFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + rfaFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + rfaFlow.allActions
		println "Missed actions: " + rfaFlow.actionsMissed
		def actionIntersection = rfaFlow.allActions - rfaFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	//Testing Power~Engineers:JPA~(1) flow
	def 'Power Engineers: PEI: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js Power~Engineers ' + peiFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		peiFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		peiFlow.formsMissed = missedList

		then:
		peiFlow.formsMissed.size() == 0

	}

	def 'Power Engineers: PEI: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  peiFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + peiFlow.allForms
		println "Missed Forms: " + peiFlow.formsMissed
		def formIntersection = peiFlow.allForms - peiFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'Power Engineers: PEI: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js Power~Engineers ' + peiFlow.flowName + ' ' + peiFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		peiFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		peiFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'Power Engineers: PEI: Generated all actions'() {

		println "args: " + peiFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + peiFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + peiFlow.allActions
		println "Missed actions: " + peiFlow.actionsMissed
		def actionIntersection = peiFlow.allActions - peiFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



	//Testing SCE_Demo:JPA~(1) flow
	def 'SCE_Demo: Pole Load Analysis: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SCE_Demo ' + poleLoadFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		poleLoadFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		poleLoadFlow.formsMissed = missedList

		then:
		poleLoadFlow.formsMissed.size() == 0

	}

	def 'SCE_Demo: Pole Load Analysis: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  poleLoadFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + poleLoadFlow.allForms
		println "Missed Forms: " + poleLoadFlow.formsMissed
		def formIntersection = poleLoadFlow.allForms - poleLoadFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'SCE_Demo: Pole Load Analysis: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SCE_Demo ' + poleLoadFlow.flowName + ' ' + poleLoadFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		poleLoadFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		poleLoadFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'SCE_Demo: Pole Load Analysis: Generated all actions'() {

		println "args: " + poleLoadFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + poleLoadFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + poleLoadFlow.allActions
		println "Missed actions: " + poleLoadFlow.actionsMissed
		def actionIntersection = poleLoadFlow.allActions - poleLoadFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}

	//Testing SPIDA:JPA~(1) flow
	def 'SPIDA: JPA old: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + oldJPAflow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		oldJPAflow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		oldJPAflow.formsMissed = missedList

		then:
		oldJPAflow.formsMissed.size() == 0

	}

	def 'SPIDA: JPA old: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  oldJPAflow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + oldJPAflow.allForms
		println "Missed Forms: " + oldJPAflow.formsMissed
		def formIntersection = oldJPAflow.allForms - oldJPAflow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'SPIDA: JPA old: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' + oldJPAflow.flowName + ' ' + oldJPAflow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		oldJPAflow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		oldJPAflow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'SPIDA: JPA old: Generated all actions'() {

		println "args: " + oldJPAflow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + oldJPAflow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + oldJPAflow.allActions
		println "Missed actions: " + oldJPAflow.actionsMissed
		def actionIntersection = oldJPAflow.allActions - oldJPAflow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	//Testing TECO: JU- UPDATED flow
	def 'TECO: JU- UPDATED: Captured all forms'() {

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js TECO ' + juFlow.formArguments).execute()
		getForms.waitForProcessOutput(System.out, System.err)

		def missedList = []

		when:
		juFlow.allForms.each{
			def htmlFile = new File('Resources/formHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		juFlow.formsMissed = missedList

		then:
		juFlow.formsMissed.size() == 0

	}

	def 'TECO: JU- UPDATED: Generated all forms'() {

		def formatFormHtml = ('casperjs generateformhtml.js ' +  juFlow.formArguments).execute()
		formatFormHtml.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All forms: " + juFlow.allForms
		println "Missed Forms: " + juFlow.formsMissed
		def formIntersection = juFlow.allForms - juFlow.formsMissed

		when:
		formIntersection.each{
			def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}


	def 'TECO: JU- UPDATED: Captured all actions'() {

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js TECO ' + juFlow.flowName + ' ' + juFlow.actionArguments).execute()
		getActions.waitForProcessOutput(System.out, System.err)

		def missedList = [];

		when:
		juFlow.allActions.each{
			def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}

		juFlow.actionsMissed = missedList
		then:
		missedList.size() == 0
	}

	def 'TECO: JU- UPDATED: Generated all actions'() {

		println "args: " + juFlow.actionArguments
		def generateActions = ('casperjs generateactionhtml.js ' + juFlow.actionArguments).execute()
		generateActions.waitForProcessOutput(System.out, System.err)

		def missedList = []

		println "All actions: " + juFlow.allActions
		println "Missed actions: " + juFlow.actionsMissed
		def actionIntersection = juFlow.allActions - juFlow.actionsMissed

		when:
		actionIntersection.each{
			def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
			if (htmlFile.length() < 2){
				missedList << it.toString()
			}
		}
		then:
		missedList.size() == 0
	}



// Testing SPIDA:Friday flow
// def 'SPIDA: Friday: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + fridayFlow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	fridayFlow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	fridayFlow.formsMissed = missedList
//
// 	then:
// 	fridayFlow.formsMissed.size() == 0
//
// }
//
// def 'SPIDA: Friday: Generated all forms'() {
//
// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  fridayFlow.formArguments).execute()
// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All forms: " + fridayFlow.allForms
// 	println "Missed Forms: " + fridayFlow.formsMissed
// 	def formIntersection = fridayFlow.allForms - fridayFlow.formsMissed
//
// 	when:
// 	formIntersection.each{
// 		def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
//
// def 'SPIDA: Friday: Captured all actions'() {
//
// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' + fridayFlow.flowName + ' ' + fridayFlow.actionArguments).execute()
// 	getActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = [];
//
// 	when:
// 	fridayFlow.allActions.each{
// 		def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
//
// 	fridayFlow.actionsMissed = missedList
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: Friday: Generated all actions'() {
//
// 	println "args: " + fridayFlow.actionArguments
// 	def generateActions = ('casperjs generateactionhtml.js ' + fridayFlow.actionArguments).execute()
// 	generateActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All actions: " + fridayFlow.allActions
// 	println "Missed actions: " + fridayFlow.actionsMissed
// 	def actionIntersection = fridayFlow.allActions - fridayFlow.actionsMissed
//
// 	when:
// 	actionIntersection.each{
// 		def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
//
//
// //Testing SPIDA:Tester flow
// def 'SPIDA: Tester: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + testerFlow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	testerFlow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	testerFlow.formsMissed = missedList
//
// 	then:
// 	testerFlow.formsMissed.size() == 0
//
// }
//
// def 'SPIDA: Tester: Generated all forms'() {
//
// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  testerFlow.formArguments).execute()
// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All forms: " + testerFlow.allForms
// 	println "Missed Forms: " + testerFlow.formsMissed
// 	def formIntersection = testerFlow.allForms - testerFlow.formsMissed
//
// 	when:
// 	formIntersection.each{
// 		def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
//
// def 'SPIDA: Tester: Captured all actions'() {
//
// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' + testerFlow.flowName + ' '  + testerFlow.actionArguments).execute()
// 	getActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = [];
//
// 	when:
// 	testerFlow.allActions.each{
// 		def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
//
// 	testerFlow.actionsMissed = missedList
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: Tester: Generated all actions'() {
//
// 	println "args: " + testerFlow.actionArguments
// 	def generateActions = ('casperjs generateactionhtml.js ' + testerFlow.actionArguments).execute()
// 	generateActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All actions: " + testerFlow.allActions
// 	println "Missed actions: " + testerFlow.actionsMissed
// 	def actionIntersection = testerFlow.allActions - testerFlow.actionsMissed
//
// 	when:
// 	actionIntersection.each{
// 		def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: ActionsGalore: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + actionsGaloreFlow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	actionsGaloreFlow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	actionsGaloreFlow.formsMissed = missedList
//
// 	then:
// 	actionsGaloreFlow.formsMissed.size() == 0
//
// }
//
// def 'SPIDA: ActionsGalore: Generated all forms'() {
//
// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  actionsGaloreFlow.formArguments).execute()
// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All forms: " + actionsGaloreFlow.allForms
// 	println "Missed Forms: " + actionsGaloreFlow.formsMissed
// 	def formIntersection = actionsGaloreFlow.allForms - actionsGaloreFlow.formsMissed
//
// 	when:
// 	formIntersection.each{
// 		def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: ActionsGalore: Captured all actions'() {
//
// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' +  actionsGaloreFlow.flowName + ' '  + actionsGaloreFlow.actionArguments).execute()
// 	getActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = [];
//
// 	when:
// 	actionsGaloreFlow.allActions.each{
// 		def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
//
// 	actionsGaloreFlow.actionsMissed = missedList
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: ActionsGalore: Generated all actions'() {
//
// 	println "args: " + actionsGaloreFlow.actionArguments
// 	def generateActions = ('casperjs generateactionhtml.js ' + actionsGaloreFlow.actionArguments).execute()
// 	generateActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All actions: " + actionsGaloreFlow.allActions
// 	println "Missed actions: " + actionsGaloreFlow.actionsMissed
// 	def actionIntersection = actionsGaloreFlow.allActions - actionsGaloreFlow.actionsMissed
//
// 	when:
// 	actionIntersection.each{
// 		def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
//
// //NC Flow test
// def 'SPIDA: NCflow: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + NCflow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	NCflow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	NCflow.formsMissed = missedList
//
// 	then:
// 	NCflow.formsMissed.size() == 0
//
// }
//
// def 'SPIDA: NCflow: Generated all forms'() {
//
// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  NCflow.formArguments).execute()
// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All forms: " + NCflow.allForms
// 	println "Missed Forms: " + NCflow.formsMissed
// 	def formIntersection = NCflow.allForms - NCflow.formsMissed
//
// 	when:
// 	formIntersection.each{
// 		def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: NCflow: Captured all actions'() {
//
// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' + NCflow.flowName + ' ' + NCflow.actionArguments).execute()
// 	getActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = [];
//
// 	when:
// 	NCflow.allActions.each{
// 		def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
//
// 	NCflow.actionsMissed = missedList
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: NCflow: Generated all actions'() {
//
// 	println "args: " + NCflow.actionArguments
// 	def generateActions = ('casperjs generateactionhtml.js ' + NCflow.actionArguments).execute()
// 	generateActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All actions: " + NCflow.allActions
// 	println "Missed actions: " + NCflow.actionsMissed
// 	def actionIntersection = NCflow.allActions - NCflow.actionsMissed
//
// 	when:
// 	actionIntersection.each{
// 		def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
//
// //NCTestFlow test
// def 'SPIDA: NCTestflow: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + NCTestflow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	NCTestflow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	NCTestflow.formsMissed = missedList
//
// 	then:
// 	NCTestflow.formsMissed.size() == 0
//
// }
//
// def 'SPIDA: NCTestflow: Generated all forms'() {
//
// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  NCTestflow.formArguments).execute()
// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All forms: " + NCTestflow.allForms
// 	println "Missed Forms: " + NCTestflow.formsMissed
// 	def formIntersection = NCTestflow.allForms - NCTestflow.formsMissed
//
// 	when:
// 	formIntersection.each{
// 		def htmlFile = new File('Resources/formHtmls/formPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: NCTestflow: Captured all actions'() {
//
// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js SPIDA ' + NCTestflow.flowName + ' ' + NCTestflow.actionArguments).execute()
// 	getActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = [];
//
// 	when:
// 	NCTestflow.allActions.each{
// 		def htmlFile = new File('Resources/actionHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
//
// 	NCTestflow.actionsMissed = missedList
// 	then:
// 	missedList.size() == 0
// }
//
// def 'SPIDA: NCTestflow: Generated all actions'() {
//
// 	println "args: " + NCTestflow.actionArguments
// 	def generateActions = ('casperjs generateactionhtml.js ' + NCTestflow.actionArguments).execute()
// 	generateActions.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	println "All actions: " + NCTestflow.allActions
// 	println "Missed actions: " + NCTestflow.actionsMissed
// 	def actionIntersection = NCTestflow.allActions - NCTestflow.actionsMissed
//
// 	when:
// 	actionIntersection.each{
// 		def htmlFile = new File('Resources/actionHtmls/actionPNGs/' + it + '.png')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	then:
// 	missedList.size() == 0
// }

// Testing SPIDA:Test-Flow flow
// def 'SPIDA: Test-Flow: Captured all forms'() {
//
// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js SPIDA ' + dlcFlow.formArguments).execute()
// 	getForms.waitForProcessOutput(System.out, System.err)
//
// 	def missedList = []
//
// 	when:
// 	dlcFlow.allForms.each{
// 		def htmlFile = new File('Resources/formHtmls/' + it + '.html')
// 		if (htmlFile.length() < 2){
// 			missedList << it.toString()
// 		}
// 	}
// 	dlcFlow.formsMissed = missedList
//
// 	then:
// 	dlcFlow.formsMissed.size() == 0
//
// }
//



















}
