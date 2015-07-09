package flow

import static groovyx.javafx.GroovyFX.*
import groovy.util.AntBuilder
import org.apache.tools.ant.taskdefs.condition.Os
import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import javafx.collections.*
import static groovyx.gpars.GParsPool.withPool


public class GUI2{

	private static void unzipFile(flowName){
		def builder = new AntBuilder()
		def dir = System.properties['user.home'] + '/Downloads/'
		flowName = flowName.replace('.flow', '')
		dir = dir + flowName + '.flow'
		builder.unzip( src: dir,  dest: "Resources/", overwrite:true )
		File jsonFile = new File('Resources/Flow.json')
        jsonFile.renameTo('Resources/' + flowName + '.json' )

		builder.delete(dir: 'Resources/actions/', failonerror: false)
		builder.delete(dir: 'Resources/conditions/', failonerror: false)
	}


	private static void showInBrowser(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            'cmd /c start Resources/Events/eventIndex.html'.execute()
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

	private static void captureFormsAndUpdateFormTable(def sampleFlow, def companyName, def formTable){

		HashMap<String, LoadItem> formMap = new HashMap()
		def formNames = sampleFlow.allForms
		formNames.each() { form ->
			formMap << [ (form.toString()) : ( new LoadItem(name: form.toString(), status: 'Unfinished', color: 'RED') )]
		}
		refreshTable(formMap, formTable)

		def getForms = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true formtohtml.js ' + companyName + ' ' + sampleFlow.formArguments).execute()
		getForms.in.eachLine { line ->
			if (line.contains('Stored screenshot of form: ')) {
				println line
				String tempName = line.replace('Stored screenshot of form: ', '')
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
				formMap.get(tempName).status = 'Ready To Export'
			} else if (line.contains('Could not find form: ')){
				String tempName = line.replace('Could not find form: ', '')
				println line
				formMap.get(tempName).status = 'Missing Form'
			}
		}
		refreshTable(formMap, formTable)
	}


	private static void captureActionsAndUpdateActionTable(def sampleFlow, def companyName, def actionTable){

		HashMap<String, LoadItem> actionMap = new HashMap()
		def actionNames = sampleFlow.allActions
		actionNames.each() { action ->
			actionMap << [ (action.toString()) : ( new LoadItem(name: action.toString(), status: 'Unfinished', color: 'RED') )]
		}
		refreshTable(actionMap, actionTable)

		def getActions = ('casperjs --ssl-protocol="any" --ignore-ssl-errors=true actiontohtml.js ' + companyName + ' ' + sampleFlow.flowName + ' ' + sampleFlow.actionArguments).execute()
		getActions.in.eachLine { line ->
			if (line.contains('Stored screenshot of action: ')) {
				println line
				String tempName = line.replace('Stored screenshot of action: ', '')
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
				actionMap.get(tempName).status = 'Ready To Export'
			}  else if (line.contains('Could not find action: ')){
				String tempName = line.replace('Could not find action: ', '')
				println line
				actionMap.get(tempName).status = 'Missing action'
			}
		}
		refreshTable(actionMap, actionTable)

	}

	private static void createEventDocuments(fileName, companyName){
		WorkFlow newFlow = new WorkFlow(fileName)
		File flowInfo = new File('Resources/flowInfo.txt')
		new File('Resources/Events/texts').mkdir()

		newFlow.events.each{k,v -> v.printEventPage(new File('Resources/Events/' + v.eventName + '.html'))}

		newFlow.printEventIndexPage(new File('Resources/Events/EventIndex.html') )

		newFlow.events.each{k,v ->
			File eventPage = new File('Resources/Events/texts/' + v.eventName + '.txt')
			v.printEventInfoPage(eventPage)
		}

		flowInfo.write(newFlow.flowName + '\n')
		flowInfo.append(companyName.replaceAll('~', ' '))

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

		def forms =  [new LoadItem(name: "No forms yet", status: 'N/A', color: 'RED')]
		def actions = [new LoadItem(name: "No actions yet", status: 'N/A', color: 'RED')]

		start {
		    stage(id: 'mainStage', title: "Workflow Document Generator", width: 600, height: 800, visible: true) {
		        scene(fill: GROOVYBLUE) {
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


							def fileName = nameOfFlowFile.text + '.json'
							SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
							def companyName = nameOfCompany.text.replaceAll(' ', '~')


							loadingForms.show()
							captureFormsAndUpdateFormTable(sampleFlow, companyName, formTable)

							loadingActions.show()
							captureActionsAndUpdateActionTable(sampleFlow, companyName, actionTable)

							createEventDocuments(fileName, companyName)

							browserButton.setDisable(false)
							exportButton.setDisable(false)

						})
					}
				}
		    }
			stage(primary: false, id: 'loadingForms', title: "Form Statuses", width: 500, height: 600, visible: false) {
				scene(fill: GROOVYBLUE) {
					tableView(id: 'formTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: forms) {
						tableColumn(id: 'formNameColumn', editable: false,  property: 'name', text: "Form Name", width: 350)
						tableColumn(id: 'formStatusColumn', editable: false,  property: 'status', text: "Status", width: 150, style: 'align: center')
					}

				}
			}

			stage(primary: false, id: 'loadingActions', title: "Action Statuses:", width: 500, height: 600, visible: false) {
				scene(fill: GROOVYBLUE) {
					tableView(id: 'actionTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: actions) {
						tableColumn(id: 'actionNameColumn', editable: false,  property: 'name', text: "Action Name", width: 350)
						tableColumn(id: 'actionStatusColumn', editable: false,  property: 'status', text: "Status", width: 150, style: 'align: center')
					}
				}
			}
		}
	}
}




// if (nameOfFlowFile.text != ''){
// 	unzipFile(nameOfFlowFile.text)
// 	if (nameOfCompany.text != ''){
// 		actionTab.setDisable(false)
// 		formTab.setDisable(false)
//
// 		def fileName = nameOfFlowFile.text + '.json'
// 		SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
// 		def companyName = nameOfCompany.text.replaceAll(' ', '~')
//
// 		captureFormsAndUpdateFormTable(sampleFlow, companyName, formTable)
// 		captureActionsAndUpdateActionTable(sampleFlow, companyName, actionTable)
//
// 		createEventDocuments(fileName, companyName)
//
// 		browserButton.setDisable(false)
// 		exportButton.setDisable(false)
// 	}else {
// 		//popup("Please enter a company name.")
// 	}
// }else {
// 	println "Please enter a .flow file name."
// }
// })
//
// button("Go to Google drive", row: 7, column: 1, halignment: "right", onAction: {
// openGoogleDrive()
// })
// }
// }
// tab('Forms', id: 'formTab', closable: false) {
// tableView(id: 'formTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: forms) {
// tableColumn(id: 'formNameColumn', editable: false,  property: 'name', text: "Form Name", width: 400)
// tableColumn(id: 'formStatusColumn', editable: false,  property: 'status', text: "Status", width: 200, style: 'align: center')
// }
// }
// formTab.setDisable(false)
//
// tab('Actions', id: 'actionTab', closable: false) {
// tableView(id: 'actionTable', selectionMode: "single", cellSelectionEnabled: true, editable: false, items: actions) {
// tableColumn(id: 'actionNameColumn', editable: false,  property: 'name', text: "Action Name", width: 400)
// tableColumn(id: 'actionStatusColumn', editable: false,  property: 'status', text: "Status", width: 200, style: 'align: center')
// }
// }
// actionTab.setDisable(false)
// }
// }
