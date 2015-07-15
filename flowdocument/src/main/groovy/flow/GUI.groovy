package flow

import static groovyx.javafx.GroovyFX.*
import groovy.util.AntBuilder
import org.apache.tools.ant.taskdefs.condition.Os
import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import javafx.collections.*
import groovyx.javafx.SceneGraphBuilder
import javafx.scene.paint.Color
import javafx.scene.text.Font
import java.util.concurrent.FutureTask
import javafx.concurrent.Task
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.shape.Rectangle
import java.net.URLEncoder

public class GUI{

	private static final Color RED = Color.web("#800000")
	private static final Color GREEN = Color.web("#00B800")
	private static final Color TERRA = Color.web("#CDB380")
	private static final Color SMOOTHBLUE = Color.web("#0A516D")
	private static final Color DARK = Color.web("#2B2726")
	private static final Color TERRATERRA = Color.web("#E8DDCB")


	private static boolean unzipFile(flowName){
		def foundFile
		def builder = new AntBuilder()
		def dir = System.properties['user.home'] + '/Downloads/'
		flowName = flowName.replace('.flow', '')
		dir = dir + flowName + '.flow'
		if ( (new File(dir)).exists() ){
			builder.unzip( src: dir,  dest: "build/Resources/", overwrite:true )
			File jsonFile = new File('build/Resources/Flow.json')
	        jsonFile.renameTo('build/Resources/' + flowName + '.json' )
			builder.delete(dir: 'build/Resources/actions/', failonerror: false)
			builder.delete(dir: 'build/Resources/conditions/', failonerror: false)
			foundFile = true
		} else {
			foundFile = false;
		}

		return foundFile
	}

	private static void showInBrowser(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            'cmd /c start build/Resources/Events/eventIndex.html'.execute()
        } else {
            def thisDirectory = System.getProperty("user.dir")
            java.awt.Desktop.desktop.browse( ('File://' + thisDirectory + '/build/Resources/Events/eventIndex.html').toURI() )
        }
	}

	private static void exportWorkflow(){
		String homeDirectory = System.properties['user.home']
	    def folderString = homeDirectory + '/Desktop/ExportToGoogleDrive'
        File googleDriveFolder = new File(folderString)


		def ant = new AntBuilder()
		def resources = ant.fileScanner {
			fileset(dir:"build/Resources") {
				include(name: '**/*.html')
				include(name: '**/*.txt')
				include(name: '**/*.png')
				exclude(name: '**/*.json')
				exclude(name: 'eventIndex.html')
			}
		}

		ant.delete(dir: googleDriveFolder,failonerror:false)
		ant.mkdir(dir: folderString)
		ant.copy(file: 'src/Resources/SpidaEmblem.png', toFile: folderString + '/Resources/SpidaEmblem.png' )
		def workingDir = System.getProperty("user.dir")
		String noColonFileString, fileString

		resources.each() { file ->
			fileString = file.toString()
			noColonFileString = folderString + (fileString - workingDir) - 'build/' - '\\build'
			ant.copy(file: file, toFile: noColonFileString , includeEmptyDirs: false, )
		}
	}

	private static void cleanWorkspace(){
		def ant = new AntBuilder()
		ant.delete(failonerror: false, dir: 'build/Resources/')
	}

	private static void openGoogleDrive(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			'cmd /c start https://www.google.com/drive/'.execute()
		} else {
			def thisDirectory = System.getProperty("user.dir")
			java.awt.Desktop.desktop.browse( 'https://www.google.com/drive/'.toURI() )
		}
	}

	private static void captureActionsAndUpdateActionTable(Thread fxThread, def mainScene, def sampleFlow, def fileName, def companyName, def actionTable, def companyPopup, def loginPopup){

		Map<String, LoadItem> actionMap = new HashMap()
		def actionNames = sampleFlow.allActions
		actionNames.each() { action ->
			actionMap << [ (action.toString()) : ( new LoadItem(name: action.toString(), status: 'Unfinished', color: RED) )]

		}

		int row = 3
		Rectangle rect
		actionMap.each {k,v ->
			String key = k.replaceAll(' ', '~')
			key = key.replaceAll(':','+')
			key = key.replaceAll('"', 'q')

			actionTable.add(new Label( text: k, id: key + 'Status', style: '-fx-font-weight: bold', textFill: Color.web('#800000')), 0, row)
			row++
		}

		Closure getActions = {

			println "Starting script"
			def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
			companyName = URLEncoder.encode(companyName, "UTF-8")
			String[] actionArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'actiontohtml.js', companyName, flowName] as String[]
			actionArgsArray = actionArgsArray + (sampleFlow.actionArguments as String[])

			def getActions = actionArgsArray.execute()
			getActions.in.eachLine { line ->
				if (line.contains('CasperError: Errors encountered while filling form: form not found')){
					println line
					getForms.destroy()
					//loginPopup.show()
				} else if (line.contains('Stored screenshot of action: ')) {
					println line
					String tempName = line.replace('Stored screenshot of action: ', '')
					def key = tempName.replaceAll(' ', '~')
					key = key.replaceAll(':','+')
					key = key.replaceAll('"', 'q')

					Label actionStatus = (Label) mainScene.lookup('#' + key + 'Status')
					if (!actionStatus.is(null)){
						actionStatus.setTextFill( Color.web('#FF7519') )
					}
				} else if (line.contains('CasperError: Cannot dispatch mousedown event on nonexistent selector: xpath selector')) {
					println line
					getForms.destroy()
					companyPopup.show()
				} else {
					println line
				}
			}
			createEventDocuments(fileName, companyName)

			String[] genActionArray = ['casperjs', 'generateactionhtml.js'] as String[]
			genActionArray = genActionArray + sampleFlow.actionArguments
			def formatActionHtml = genActionArray.execute()
			formatActionHtml.in.eachLine { line ->
				if (line.contains('Created PNG of: ')) {
					println line
					String tempName = line.replace('Created PNG of: ', '')
					def key = tempName.replaceAll(' ', '~')
					key = key.replaceAll(':','+')
					key = key.replaceAll('"', 'q')

					Label actionStatus = (Label) mainScene.lookup('#' + key + 'Status')
					if (!actionStatus.is(null)){
						actionStatus.setTextFill( Color.web('#004700') )
					}
				} else if (line.contains('Could not find action: ')){
					println line
					String tempName = line.replace('Could not find action: ', '')
					def key = tempName.replaceAll(' ', '~')
					key = key.replaceAll(':','+')
					key = key.replaceAll('"', 'q')

					Label actionStatus = (Label) mainScene.lookup('#' + key + 'Status')
					if (!actionStatus.is(null)){
						actionStatus?.setTextFill( Color.web('#800000') )
					}
				} else {
					println line
				}
			}
		}
		Thread actionThread = new Thread(getActions as Runnable)
		actionThread.setDaemon(true)
		actionThread.start()
	}

	private static void captureFormsAndUpdateFormTable(Thread fxThread, def mainScene, def sampleFlow, def companyName, def formTable, def companyPopup, def loginPopup){

			HashMap<String, LoadItem> formMap = new HashMap()
			def formNames = sampleFlow.allForms
			formNames.each() { form ->
				formMap << [ (form.toString()) : ( new LoadItem(name: form.toString(), status: 'Unfinished', color: RED) )]
			}

			int row = 3
			Rectangle rect
			formMap.each {k,v ->
				String key = k.replaceAll(' ', '~')
				key = key.replaceAll(':','+')
				key = key.replaceAll('"', 'q')

				formTable.add(new Label( text: k, id: key + 'Status', style: '-fx-font-weight: bold', textFill: Color.web('#800000')), 0, row)
				row++
			}

			Closure getForms = {

				println "Starting script"

				def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
				companyName = URLEncoder.encode(companyName, "UTF-8")
				String[] formArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'formtohtml.js', companyName] as String[]
				formArgsArray = formArgsArray + (sampleFlow.formArguments as String[])

				def getForms = formArgsArray.execute()
				getForms.in.eachLine { line ->
					if (line.contains('CasperError: Errors encountered while filling form: form not found')){
						println line
						getForms.destroy()
						loginPopup.show()
					} else if (line.contains('Stored screenshot of form: ')) {
						println line
						String tempName = line.replace('Stored screenshot of form: ', '')
						def key = tempName.replaceAll(' ', '~')
						key = key.replaceAll(':','+')
						key = key.replaceAll('"', 'q')

						Label formStatus = (Label) mainScene.lookup('#' + key + 'Status')
	                    formStatus?.setTextFill( Color.web('#FF7519') )
					} else if (line.contains('CasperError: Cannot dispatch mousedown event on nonexistent selector: xpath selector')) {
						companyPopup.show()
						getForms.destroy()
						println line
					} else {
						println line
					}
				}

				String[] genFormArray = ['casperjs', 'generateformhtml.js'] as String[]
				genFormArray = genFormArray + sampleFlow.formArguments
				def formatFormHtml = genFormArray.execute()
				formatFormHtml.in.eachLine { line ->
					if (line.contains('Created PNG of: ')) {
						println line
						String tempName = line.replace('Created PNG of: ', '')
						def key = tempName.replaceAll(' ', '~')
						key = key.replaceAll(':','+')
						key = key.replaceAll('"', 'q')
						Label formStatus = (Label) mainScene.lookup('#' + key + 'Status')
	                    formStatus?.setTextFill( Color.web('#004700') )
					} else if (line.contains('Could not find form: ')){
						println line
						String tempName = line.replace('Could not find form: ', '')
						def key = tempName.replaceAll(' ', '~')
						key = key.replaceAll(':','+')
						key = key.replaceAll('"', 'q')

						Label formStatus = (Label) mainScene.lookup('#' + key + 'Status')
	                    formStatus?.setTextFill( Color.web('#800000') )
					} else {
						println line
					}
				}
			}

		Thread formThread = new Thread(getForms as Runnable)
		formThread.setDaemon(true)
		formThread.start()
		//return formThread
	}

	private static void createEventDocuments(fileName, companyName){

		WorkFlow newFlow = new WorkFlow(fileName)
		File flowInfo = new File('build/Resources/flowInfo.txt')
		new File('build/Resources/Events/texts').mkdir()

		newFlow.events.each{k,v -> v.printEventPage(new File('build/Resources/Events/' + v.eventName + '.html'))}

		newFlow.printEventIndexPage(new File('build/Resources/Events/EventIndex.html') )

		newFlow.events.each{k,v ->
			File eventPage = new File('build/Resources/Events/texts/' + v.eventName + '.txt')
			v.printEventInfoPage(eventPage)
		}

		flowInfo.write(newFlow.flowName + '\n')
		flowInfo.append(companyName.replaceAll('~', ' '))
	}

	public static void main(args){

		final HashMap<String, LoadItem> forms =  ['None' : new LoadItem(name: "No forms yet", status: 'N/A', color: RED)]
		final HashMap<String, LoadItem> actions = ['None' : new LoadItem(name: "No actions yet", status: 'N/A', color: RED)]

		start {
		    stage(id: 'mainStage', title: "Workflow Document Generator", width: 800, height: 450, visible: true) {
				scene(id: 'mainScene'){
					File css = new File("src/main/groovy/flow/resources/gui.css")
					mainScene.getStylesheets().clear()
					mainScene.getStylesheets().add("file:///" + css.getAbsolutePath().replace("\\", "/"))
					tabPane (id: 'tabPane') {
		                tab('Main', id: 'mainTab', closable: false) {
		                    gridPane(id: 'gridPane', hgap: 5, vgap: 15, padding: 5, alignment: "top_center", style: '-fx-background-color: #2B2726') {
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')


								label("Enter Workflow Parameters", id: 'titleLabel', row: 0, column: 0, columnSpan: 8, halignment: "center",
									margin: [0, 0, 10] )

								label("Flow Name", id: 'flowNameField', hgrow: "never", style: '-fx-font-size: 14', row: 1, column: 2, columnSpan: 1, halignment: "right", textFill: TERRA)
								textField(promptText: ".flow file name", id: 'nameOfFlowFile', row: 1, column: 3, columnSpan: 3, halignment: "left")

								label("Company", row: 2, column: 2, columnSpan: 1, textFill: TERRA, style: '-fx-font-size: 14', halignment: "right")
								textField(promptText: "Company Name", id: 'nameOfCompany', row: 2, column: 3, columnSpan: 3, halignment: "left")

								label(" ", id: 'filler', row: 4, column: 7)

								button("Clean Workspace", id: 'cleanButton',  minWidth: 175, prefWidth: 175, row: 7, column: 1, columnSpan: 2, halignment: "center",
								, onAction: {
									cleanWorkspace()
									browserButton.setDisable(true)
									exportButton.setDisable(true)
								})

								label("Gather: ",  textFill: SMOOTHBLUE, style: '-fx-font-size: 14', row: 5, column: 0, columnSpan: 1, halignment: "right" )
								label("Produce: ",  textFill: SMOOTHBLUE, style: '-fx-font-size: 14', row: 6, column: 0, columnSpan: 1, halignment: "right" )
								label("Utilities: ", textFill: SMOOTHBLUE, style: '-fx-font-size: 14', row: 7, column: 0, columnSpan: 1, halignment: "right" )



								button("All Documents", id: 'genDocButton', minWidth: 175, prefWidth: 175, row: 5, column: 5, columnSpan: 2, halignment: "center", onAction: {

									if(  unzipFile(nameOfFlowFile.text) ){

										if ( !( new File('build/Resources/Events/eventIndex.html') ).exists()  ){

											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											Thread fxThread = Thread.currentThread()
											captureFormsAndUpdateFormTable(fxThread, mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)
											captureActionsAndUpdateActionTable(fxThread, mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)

											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

										} else {
											cleanFirstPopup.show()
											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											Thread fxThread = Thread.currentThread()
											captureFormsAndUpdateFormTable(fxThread, mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)
											captureActionsAndUpdateActionTable(fxThread, mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)


											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)
										}

									}else {
										flowPopup.show()
									}
								})

								button("Forms", id: 'genFormsButton',  minWidth: 175, prefWidth: 175, row: 5, column: 3, columnSpan: 2, halignment: "center", onAction: {

									if(  unzipFile(nameOfFlowFile.text) ){

										def fileName = nameOfFlowFile.text + '.json'
										SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
										def companyName = nameOfCompany.text.replaceAll(' ', '~')

										tabPane.getSelectionModel().select(1)
										Thread fxThread = Thread.currentThread()
										captureFormsAndUpdateFormTable(fxThread, mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)

										browserButton.setDisable(false)
										exportButton.setDisable(false)
										cleanButton.setDisable(false)
									}else {
										flowPopup.show()
									}


								})

								button("Actions", id: 'genActionsButton', minWidth: 175, prefWidth: 175, row: 5, column: 1, columnSpan: 2, halignment: "center", onAction: {
										if(  unzipFile(nameOfFlowFile.text) ){

											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(2)
											Thread fxThread = Thread.currentThread()
											captureActionsAndUpdateActionTable(fxThread, mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)

											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

										}else {
											flowPopup.show()
										}
								})

								button("Create Export Folder", id: 'exportButton',  minWidth: 175, prefWidth: 175, row: 6, column: 1, columnSpan: 2, halignment: "center", onAction: {
									exportWorkflow()
								})

								button("Open in browser", id: 'browserButton',  minWidth: 175, prefWidth: 175, row: 6, column: 3, columnSpan: 2, halignment: "center", onAction: {
									if ( (new File('build/Resources/Events/eventIndex.html')).exists() ) {
										showInBrowser()
									} else {
										noBrowserPopup.show()
									}
								})

								button("Go to Google drive", minWidth: 175, prefWidth: 175, row: 7, column: 3, columnSpan: 2, halignment: "center", onAction: {
									openGoogleDrive()
								})


							}
						}


						tab('Forms', id: 'formTab', closable: false) {
							scrollPane(fitToWidth: true, fitToHeight: true) {

								gridPane(id: 'formTable', hgap: 0, vgap: 0, padding: 10, alignment: "top_center", style: '-fx-background-color: #E8DDCB') {
									columnConstraints(minWidth: 640, prefWidth: 640, hgrow: 'never' , halignment: 'center')
									label(text: "Forms", row: 0, column: 0, style: '-fx-font-size: 24pt; fx-font-weight: bold;', textFill: Color.web('#0A516D') )
									rectangle(width: 650, height: 4, fill: Color.web('#2B2726'), halignment: 'center', valignment: 'top', row: 1, column: 0)
									label(" ", row: 2, column: 0)
								}
							}
						}

						tab('Actions', id: 'actionTab', closable: false) {
							scrollPane(style: '-fx-background-color: #E8DDCB', fitToWidth: true, fitToHeight: true) {

								gridPane(id: 'actionTable', hgap: 0, vgap: 0, padding: 10, alignment: "top_center", style: '-fx-background-color: #E8DDCB') {
									columnConstraints(minWidth: 640, prefWidth: 640, hgrow: 'never' , halignment: 'center')
									label(text: "Actions", row: 0, column: 0, style: '-fx-font-size: 24pt; fx-font-weight: bold;', textFill: Color.web('#0A516D') )
									rectangle(width: 650, height: 4, fill: Color.web('#2B2726'), halignment: 'center', valignment: 'top', row: 1, column: 0)
									label(" ", row: 2, column: 0)
								}
							}
						}
		            }
		        }
		    }
			stage(primary: false, id: 'flowPopup', title: "ERROR: Invalid flow file", width: 400, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Invalid .flow file name.  Make sure you downloaded the \nfile and it is in your user home directory/Downloads/ folder", row: 0, column: 0)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							flowPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'companyPopup', title: "ERROR: Invalid company name", width: 400, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Invalid company name.  Check your syntax and try again.", row: 0, column: 0)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							companyPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'noBrowserPopup', title: "ERROR: Cannot show browser", width: 400, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Cannot open eventIndex.html, Have you generated \nforms/actions/both?", row: 0, column: 0)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							noBrowserPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'cleanFirstPopup', title: "WARNING: Clean Before Building", width: 500, height: 150, visible: false) {
				scene {
					gridPane(hgap: 10, vgap: 10, padding: 25, alignment: "top_center" ) {
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						label("Did you want to clean the old workflow resources before gathering new?", halignment: 'center', row: 0, column: 0, columnSpan: 2)
						button("Clean", id: 'popupClean', minWidth: 100, prefWidth: 100, row: 1, column: 0, halignment: "right", onAction: {
							cleanWorkspace()
							browserButton.setDisable(true)
							exportButton.setDisable(true)
							cleanButton.setDisable(true)
							cleanFirstPopup.hide()
						})
						button("Nope", minWidth: 100, prefWidth: 100, row: 1, column: 1, halignment: "left", onAction: {
							cleanFirstPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'loginPopup', title: "ERROR: Could not log in to spidasoftware", width: 500, height: 150, visible: false) {
				scene {
					gridPane(hgap: 10, vgap: 10, padding: 25, alignment: "top_center" ) {
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						label("Error trying to log into https://www.spidasoftware.com/projectmanager/\nCheck internet connection and try again.", halignment: 'center', row: 0, column: 0, columnSpan: 2)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							loginPopup.hide()
						})
					}
				}
			}
		}
	}
}
