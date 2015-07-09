package flow

import static groovyx.javafx.GroovyFX.*
import groovy.util.AntBuilder
import org.apache.tools.ant.taskdefs.condition.Os
import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import javafx.collections.*


public class GUI{


	// private static void runScripts(String flowName, String companyName){
	//
	// 	def fileName = flowName + '.json'
	// 	WorkFlow newFlow = new WorkFlow(fileName)
	// 	SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
	// 	File flowInfo = new File('Resources/flowInfo.txt')
	// 	new File('Resources/Events/texts').mkdir()
	//
	// 	flowInfo.write(newFlow.flowName + '\n')
	// 	flowInfo.append(companyName.replaceAll('~', ' '))
	//
	// 	def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ' + companyName + ' ' + sampleFlow.formArguments).execute()
	// 	getForms.waitForProcessOutput(System.out, System.err)
	//
	// 	def formatFormHtml = ('casperjs generateformhtml.js ' +  sampleFlow.formArguments).execute()
	// 	formatFormHtml.waitForProcessOutput(System.out, System.err)
	//
	// 	def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ' + companyName + ' ' + sampleFlow.flowName + ' ' + sampleFlow.actionArguments).execute()
	// 	getActions.waitForProcessOutput(System.out, System.err)
	//
	// 	def formatActionHtml = ('casperjs generateactionhtml.js ' + sampleFlow.actionArguments).execute()
	// 	formatActionHtml.waitForProcessOutput(System.out, System.err)
	//
	// 	newFlow.events.each{k,v -> v.printEventPage(new File('Resources/Events/' + v.eventName + '.html'))}
	//
	// 	newFlow.printEventIndexPage(new File('Resources/Events/EventIndex.html') )
	//
	// 	newFlow.events.each{k,v ->
	// 		File eventPage = new File('Resources/Events/texts/' + v.eventName + '.txt')
	// 		v.printEventInfoPage(eventPage)
	// 	}
	//
	// }



	private static void unzipFile(flowName){
		def builder = new AntBuilder()
		def dir = System.properties['user.home'] + '/Downloads/'
		flowName = flowName.replace('.flow', '')
		dir = dir + flowName + '.flow'
		println dir
		builder.unzip( src: dir,  dest: "Resources/", overwrite:true )
		File jsonFile = new File('Resources/Flow.json')
        jsonFile.renameTo('Resources/' + flowName + '.json' )

		builder.delete(dir: 'Resources/actions/', failonerror: false)
		builder.delete(dir: 'Resources/conditions/', failonerror: false)
	}


	private static void showInBrowser(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            'cmd /c start Resources/Events/eventIndex.html'.execute()
			println System.getProperty("user.dir")
        } else {
            def thisDirectory = System.getProperty("user.dir")
            java.awt.Desktop.desktop.browse( ('File://' + thisDirectory + '/Resources/Events/eventIndex.html').toURI() )
        }
	}



	private static void exportWorkflow(){
		String homeDirectory = System.properties['user.home']
	    def folderString = homeDirectory + '/Desktop/ExportToGoogleDrive'
        File googleDriveFolder = new File(folderString)


		def ant = new AntBuilder()
		def resources = ant.fileScanner {
			fileset(dir:"Resources") {
				exclude(name: '.DS_Store')
		        exclude(name: '**/*.css')
		        exclude(name: '**/*.json')
			}
		}


		ant.delete(dir: googleDriveFolder,failonerror:false)
		ant.mkdir(dir: folderString)
		def workingDir = System.getProperty("user.dir")
		String noColonFileString

		resources.each() { file ->
			String fileString = file.toString()
			noColonFileString = folderString + (fileString - workingDir)
			//noColonFileString = fileString.replaceAll(':', '[colon]')
			println noColonFileString
			ant.copy(file: file, toFile: noColonFileString , includeEmptyDirs: false, )
		}

		ant.delete(dir: folderString + '/Resources/formHtmls/images', failonerror: false)
		ant.delete(dir: folderString + '/Resources/images', failonerror: false)



	}


	private static void cleanWorkspace(){
		def ant = new AntBuilder()
		ant.delete(failonerror: false) {
			fileset(dir:"Resources/Events") {
				exclude(name: '.DS_Store')
				exclude(name: '**/*.css')
			}
		}
		ant.delete(failonerror: false) {
			fileset(dir:"Resources/formHtmls/formPNGs") {
				exclude(name: '.DS_Store')
				exclude(name: '**/*.css')
			}
		}
		ant.delete(failonerror: false) {
			fileset(dir:"Resources/actionHtmls") {
				exclude(name: '.DS_Store')
				exclude(name: '**/*.css')
			}
		}
		ant.delete(failonerror: false) {
			fileset(dir:"Resources/formHtmls") {
				exclude(name: '.DS_Store')
				exclude(name: '**/*.css')
				exclude(name: '**/*.png')
				exclude(name: '**/*.ttf')
				exclude(name: '**/*.js')
				exclude(name: '**/*.gif')
			}
		}
		ant.delete(dir: 'Resources/actions/', failonerror: false)
		ant.delete(dir: 'Resources/conditions/', failonerror: false)

	}


	private static void openGoogleDrive(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			'cmd /c start https://www.google.com/drive/'.execute()
		} else {
			def thisDirectory = System.getProperty("user.dir")
			java.awt.Desktop.desktop.browse( 'https://www.google.com/drive/'.toURI() )
		}


	}

	private static void captureFormsAndUpdateFormTable(def sampleFlow, def companyName, def formTable, def formMap){

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ' + companyName + ' ' + sampleFlow.formArguments).execute()
		getForms.in.eachLine { line ->
			if (line.contains('Stored screenshot of form: ')) {
				println line
				String tempName = line.replace('Stored screenshot of form: ', '')
				println formMap.get(tempName)
				formMap.get(tempName).status = 'Captured'
			} else {
				println line
			}
		}

		refreshTable(formMap, formTable)

		def formatFormHtml = ('casperjs generateformhtml.js ' +  sampleFlow.formArguments).execute()
		formatFormHtml.in.eachLine { line ->
			if (line.contains('Created PNG of: ')) {
				println line
				String tempName = line.replace('Created PNG of: ', '')
				println formMap.get(tempName)
				formMap.get(tempName).status = 'Ready To Export'
			} else if (line.contains('Could not find form: ')){
				String tempName = line.replace('Could not find form: ', '')
				println formMap.get(tempName)
				formMap.get(tempName).status = 'Missing Form'
			}
		}
		refreshTable(formMap, formTable)
	}


	private static void captureActionsAndUpdateActionTable(def sampleFlow, def companyName, def actionTable, def actionMap){

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ' + companyName + ' ' + sampleFlow.flowName + ' ' + sampleFlow.actionArguments).execute()
		getActions.in.eachLine { line ->
			if (line.contains('Stored screenshot of action: ')) {
				println line
				String tempName = line.replace('Stored screenshot of action: ', '')
				println actionMap.get(tempName)
				actionMap.get(tempName).status = 'Captured'
			} else {
				println line
			}
		}
		refreshTable(actionMap, actionTable)

		def formatActionHtml = ('casperjs generateactionhtml.js ' + sampleFlow.actionArguments).execute()
		formatActionHtml.in.eachLine { line ->
			if (line.contains('Created PNG of: ')) {
				println line
				String tempName = line.replace('Created PNG of: ', '')
				println actionMap.get(tempName)
				actionMap.get(tempName).status = 'Ready To Export'
			}  else if (line.contains('Could not find action: ')){
				String tempName = line.replace('Could not find action: ', '')
				println formMap.get(tempName)
				formMap.get(tempName).status = 'Missing action'
			}
		}
		refreshTable(actionMap, actionTable)

	}


	private static void refreshTable(def map, def table){
		ArrayList<LoadItem> populateTable = map.values()
		ObservableList<LoadItem> updateTable = populateTable as ObservableList
		table.items = updateTable
		table.getColumns().get(0).setVisible(false)
		table.getColumns().get(0).setVisible(true)
		table.getColumns().get(1).setVisible(false)
		table.getColumns().get(1).setVisible(true)
	}


	public static void main(args){

		def forms =  [
			new LoadItem(name: "fORM 1", status: 'unfinished', color: 'RED'),
			new LoadItem(name: "Jim Clarke", status: 'unfinished', color: 'RED'),
			new LoadItem(name: "CONWAY", status: 'unfinished', color: 'RED')
		]

		start {
		    stage(title: "Workflow Document Generator", width: 600, height: 800, visible: true) {
		        scene(fill: GROOVYBLUE) {
		            tabPane {
		                tab('Main', id: 'mainTab', closable: false) {
		                    gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center") {
								columnConstraints(minWidth: 50, halignment: "right")
								columnConstraints(prefWidth: 250, hgrow: 'always')

								label("Enter Workflow Parameters", style: "-fx-font-size: 18px;",
									textFill: black, row: 0, columnSpan: 2, halignment: "center",
									margin: [0, 0, 10] )

								label("Flow Name", hgrow: "never", row: 1, column: 0, textFill: black)
								def nameOfFlowFile = textField(promptText: ".flow file name", row: 1, column: 1 )

								label("Company", row: 2, column: 0, textFill: black)
								def nameOfCompany = textField(promptText: "Company Name", row: 2, column: 1)

								def browserButton = button("Open in browser", row: 5, column: 1, halignment: "right", onAction: {
									showInBrowser()
								})
								browserButton.setDisable(true)

								def exportButton = button("Create Export Folder", row: 6, column: 0, halignment: "right", onAction: {
									exportWorkflow()
								})
								exportButton.setDisable(true)

								button("Clean Workspace", row: 6, column: 1, halignment: "right", onAction: {
									cleanWorkspace()
									browserButton.setDisable(true)
									exportButton.setDisable(true)
								})

								button("Generate Document", id: 'genDocButton', row: 5, column: 0, halignment: "right", onAction: {

									if (nameOfFlowFile.text != ''){
										unzipFile(nameOfFlowFile.text)
										if (nameOfCompany.text != ''){
											actionTab.setDisable(false)
											formTab.setDisable(false)

		/**************************************************runScripts()**********************************************************************/
											def fileName = nameOfFlowFile.text + '.json'
											WorkFlow newFlow = new WorkFlow(fileName)
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											File flowInfo = new File('Resources/flowInfo.txt')
											new File('Resources/Events/texts').mkdir()
											def companyName = nameOfCompany.text.replaceAll(' ', '~')


											HashMap<String, LoadItem> formMap = new HashMap()
											def formNames = sampleFlow.allForms
											formNames.each() { form ->
												formMap << [ (form.toString()) : ( new LoadItem(name: form.toString(), status: 'Unfinished', color: 'RED') )]
											}
											refreshTable(formMap, formTable)
											captureFormsAndUpdateFormTable(sampleFlow, companyName, formTable, formMap)


											HashMap<String, LoadItem> actionMap = new HashMap()
											def actionNames = sampleFlow.allActions
											actionNames.each() { action ->
												actionMap << [ (action.toString()) : ( new LoadItem(name: action.toString(), status: 'Unfinished', color: 'RED') )]
											}
											refreshTable(actionMap, actionTable)
											captureActionsAndUpdateActionTable(sampleFlow, companyName, actionTable, actionMap)

											newFlow.events.each{k,v -> v.printEventPage(new File('Resources/Events/' + v.eventName + '.html'))}

											newFlow.printEventIndexPage(new File('Resources/Events/EventIndex.html') )

											newFlow.events.each{k,v ->
												File eventPage = new File('Resources/Events/texts/' + v.eventName + '.txt')
												v.printEventInfoPage(eventPage)
											}

											flowInfo.write(newFlow.flowName + '\n')
											flowInfo.append(companyName.replaceAll('~', ' '))



			/***********************************************************************************************************************/
											browserButton.setDisable(false)
											exportButton.setDisable(false)
										}else {
											//popup("Please enter a company name.")
										}
									}else {
										println "Please enter a .flow file name."
									}
								})

								button("Go to Google drive", row: 7, column: 1, halignment: "right", onAction: {
									openGoogleDrive()
								})
							}
						}
		                tab('Forms', id: 'formTab', closable: false) {
		                    tableView(id: 'formTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: forms) {
				                tableColumn(id: 'formNameColumn', editable: false,  property: 'name', text: "Form Name", width: 400)
								tableColumn(id: 'formStatusColumn', editable: false,  property: 'status', text: "Status", width: 200, style: 'align: center')
		                	}
						}
						formTab.setDisable(false)

		                tab('Actions', id: 'actionTab', closable: false) {
		                    tableView(id: 'actionTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: forms) {
				                tableColumn(id: 'actionNameColumn', editable: false,  property: 'name', text: "Action Name", width: 400)
								tableColumn(id: 'actionStatusColumn', editable: false,  property: 'status', text: "Status", width: 200, style: 'align: center')
		                	}
						}
						actionTab.setDisable(false)
		            }
		        }
		    }
		}
	}
}
