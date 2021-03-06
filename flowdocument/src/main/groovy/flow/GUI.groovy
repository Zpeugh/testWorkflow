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
import javafx.scene.layout.ColumnConstraints
import java.util.concurrent.FutureTask
import javafx.concurrent.Task
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.shape.Rectangle
import java.net.URLEncoder

public class GUI{

	private static final Color SPIDARED = Color.web("#800000")
	private static final Color SPIDAGREY = Color.web("#7C8180")


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
			def scriptDir = new File('ReadMe.pdf')
			String sDir = scriptDir.getAbsolutePath() - 'ReadMe.pdf'
			println 'file:///' + sDir + 'build/Resources/Events/eventIndex.html'
            ('cmd /c start file:\\\\\\' + sDir + 'build\\Resources\\Events\\eventIndex.html').execute()
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
				exclude(name: '.DS_STORE')
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

	private static void cleanWorkspace(def deleteJson){
		def ant = new AntBuilder()
		if (deleteJson){
			ant.delete(failonerror: false, dir: 'build/Resources/'){
				exclude(name: '**/*.json')
			}
		} else {
			ant.delete(failonerror: false, dir: 'build/Resources/')
		}
	}

	private static void openGoogleDrive(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			'cmd /c start https://www.google.com/drive/'.execute()
		} else {
			def thisDirectory = System.getProperty("user.dir")
			java.awt.Desktop.desktop.browse( 'https://www.google.com/drive/'.toURI() )
		}
	}

	private static String makeID(def name){
		def newName = URLEncoder.encode(name)
		newName = newName.replaceAll('\\.', 'p')
		return newName
	}

	private static void captureActionsAndUpdateActionTable(def mainScene, def sampleFlow, def fileName, def companyName, def actionTable, def companyPopup, def loginPopup){

		Map<String, LoadItem> actionMap = new HashMap()
		def actionNames = sampleFlow.allActions
		actionNames.each() { action ->
			actionMap << [ (action.toString()) : ( new LoadItem(name: action.toString(), status: 'Unfinished', color: SPIDARED) )]

		}

		int row = 3
		int rectNum = 0;
		def numOfRects = actionMap.size() * 2 + 4
		def rectWidth = (620 / numOfRects)

		actionMap.each { actionTable.getColumnConstraints().add( new ColumnConstraints(minWidth: rectWidth, prefWidth: rectWidth) ) }
		for (int i = 0; i < numOfRects; i++){
			actionTable.add(new Rectangle( width: rectWidth, id: 'actionRect' + i,  height: 15, fill: SPIDAGREY ), i, 1)
		}

		actionMap.each {k,v ->
			String key = makeID(k)
			actionTable.add(new Label( text: k, id: 'Action' + key, style: '-fx-font-weight: bold', textFill: Color.web('#800000'), alignment: 'CENTER' ), 0, row, numOfRects, 1)
			row++
		}

		Closure getActions = {

			println "Starting script"
			def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
			companyName = URLEncoder.encode(companyName, "UTF-8")
			String[] actionArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'actiontohtml.js', companyName, flowName] as String[]
			actionArgsArray = actionArgsArray + (sampleFlow.actionArguments as String[])

			def getActions = actionArgsArray.execute()
			rectNum = 0;
			getActions.in.eachLine { line ->
				if (line.contains('CasperError: Errors encountered while filling form: form not found')){
					println line
					getActions.destroy()
					//loginPopup.show()
				} else if (line.contains('Checkpoint reached')) {
					println line
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#037D08') )
				}else if (line.contains('Stored screenshot of action: ')) {
					println line
					String tempName = line.replace('Stored screenshot of action: ', '')
					String key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#D48600') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#037D08') )

				} else if (line.contains('CasperError: Cannot dispatch mousedown event on nonexistent selector: xpath selector')) {
					println line
					getActions.destroy()
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
					String key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#037D08') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#037D08') )

				} else if (line.contains('Could not find action: ')){
					println line
					String tempName = line.replace('Could not find action: ', '')
					def key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#800000') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#037D08') )

				} else {
					println line
				}
			}
		}
		Thread actionThread = new Thread(getActions as Runnable)
		actionThread.setDaemon(true)
		actionThread.start()
	}

	private static void captureFormsAndUpdateFormTable(def mainScene, def sampleFlow, def companyName, def formTable, def companyPopup, def loginPopup){

			HashMap<String, LoadItem> formMap = new HashMap()
			def formNames = sampleFlow.allForms
			formNames.each() { form ->
				formMap << [ (form.toString()) : ( new LoadItem(name: form.toString(), status: 'Unfinished', color: SPIDARED) )]
			}

			int row = 3
			int rectNum = 0;
			def numOfRects = formMap.size() * 2 + 5
			def rectWidth = (620 / numOfRects)

			formMap.each { formTable.getColumnConstraints().add( new ColumnConstraints(minWidth: rectWidth, prefWidth: rectWidth) ) }
			for (int i = 0; i < numOfRects; i++){
				formTable.add(new Rectangle( width: rectWidth, id: 'formRect' + i,  height: 15, fill: SPIDAGREY ), i, 1)
			}

			formMap.each {k,v ->
				String key = makeID(k)
				formTable.add(new Label( text: k, id: 'Form' + key, style: '-fx-font-weight: bold', textFill: Color.web('#800000'), alignment: 'CENTER' ), 0, row, numOfRects, 1)
				row++
			}


			Closure getForms = {

				println "Starting script"

				def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
				companyName = URLEncoder.encode(companyName, "UTF-8")
				String[] formArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'formtohtml.js', companyName] as String[]
				formArgsArray = formArgsArray + (sampleFlow.formArguments as String[])


				def getForms = formArgsArray.execute()
				rectNum = 0;
				getForms.in.eachLine { line ->
					if (line.contains('CasperError: Errors encountered while filling form: form not found')){
						println line
						getForms.destroy()
						loginPopup.show()
					} else if (line.contains('Checkpoint reached')) {
						println line
						Rectangle rectFill = (Rectangle) mainScene.lookup('#formRect' + rectNum++)
						rectFill?.setFill( Color.web('#037D08') )
					}
					else if (line.contains('Stored screenshot of form: ')) {
						println line
						String tempName = line.replace('Stored screenshot of form: ', '')
						def key = makeID(tempName)

						Label formStatus = (Label) mainScene.lookup('#' + 'Form' + key)
	                    formStatus?.setTextFill( Color.web('#D48600') )
						Rectangle rectFill = (Rectangle) mainScene.lookup('#formRect' + rectNum++)
						rectFill?.setFill( Color.web('#037D08') )

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
						def key = makeID(tempName)
						Label formStatus = (Label) mainScene.lookup('#' + 'Form' + key)
	                    formStatus?.setTextFill( Color.web('#037D08') )
						Rectangle rectFill = (Rectangle) mainScene.lookup('#formRect' + rectNum++)
						rectFill?.setFill( Color.web('#037D08') )
					} else if (line.contains('Could not find form: ')){
						println line
						String tempName = line.replace('Could not find form: ', '')
						def key = makeID(tempName)

						Label formStatus = (Label) mainScene.lookup('#' + 'Form' + key)
	                    formStatus?.setTextFill( Color.web('#800000') )
					} else {
						println line
					}
				}
			}

		Thread formThread = new Thread(getForms as Runnable)
		formThread.setDaemon(true)
		formThread.start()
	}

	private static void createEventDocuments(fileName, companyName){

		WorkFlow newFlow = new WorkFlow(fileName)
		HashMap<Integer, Event> eventMap = newFlow.events.clone()
		File flowInfo = new File('build/Resources/flowInfo.txt')
		new File('build/Resources/Events/texts').mkdir()

		newFlow.events.each{ k,v ->
			def name = v.eventName.replaceAll("'", '[squote]')
			name = name.replaceAll('\\\\', '[bslash]')
			name = name.replaceAll('\"', '[dquote]')
			name = name.replaceAll('/', '[fslash]')
			name = name.replaceAll(':', '[colon]')
			name = name.replaceAll('\\?', '[question]')
			File file = new File('build/Resources/Events/' + name + '.html')
			v.printEventPage(file, eventMap)
		}

		newFlow.printEventIndexPage(new File('build/Resources/Events/EventIndex.html') )

		newFlow.events.each{k,v ->

			def name = v.eventName.replaceAll('\'', '[squote]')
			name = name.replaceAll('\\\\', '[bslash]')
			name = name.replaceAll('\"', '[dquote]')
			name = name.replaceAll('/', '[fslash]')
			name = name.replaceAll(':', '[colon]')
			name = name.replaceAll('\\?', '[question]')
			File eventPage = new File('build/Resources/Events/texts/' + name + '.txt')

			v.printEventInfoPage(eventPage, eventMap)
		}

		flowInfo.write(newFlow.flowName + '\n')
		flowInfo.append(companyName.replaceAll('~', ' '))
	}

	public static void main(args){

		final HashMap<String, LoadItem> forms =  ['None' : new LoadItem(name: "No forms yet", status: 'N/A', color: SPIDARED)]
		final HashMap<String, LoadItem> actions = ['None' : new LoadItem(name: "No actions yet", status: 'N/A', color: SPIDARED)]

		start {
		    stage(id: 'mainStage', title: "Workflow Document Generator", width: 800, height: 450, visible: true) {
				scene(id: 'mainScene'){
					File css = new File("src/main/groovy/flow/resources/gui.css")
					mainScene.getStylesheets().clear()
					mainScene.getStylesheets().add("file:///" + css.getAbsolutePath().replaceAll('\\\\', "/"))
					tabPane (id: 'tabPane', style: '-fx-background-color: #000000;-fx-border-color: #7C8180;-fx-border-width: 0px 1px 1px 1px;') {
		                tab(' Main ', id: 'mainTab', closable: false) {
		                    gridPane(id: 'gridPane', hgap: 5, vgap: 15, padding: 0, alignment: "top_center", style: '-fx-background-color: #000000') {
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')


								label("Enter Workflow Parameters", valignment: 'bottom', id: 'titleLabel', row: 0, column: 2, columnSpan: 6, halignment: "center",
									margin: [0, 0, 10] )

								imageView(row: 0, column: 0, columnSpan: 2, rowSpan: 3, fitWidth: 275, preserveRatio: true){
									def img = new File('src/Resources/SpidaLogo.png')
									image('file:///' + img.getAbsolutePath().replace("\\", "/"))
								}
								label("Flow Name ", id: 'flowNameField', hgrow: "never", style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 1, column: 3, columnSpan: 1, valignment: 'bottom', halignment: "center", textFill: SPIDAGREY)
								textField(promptText: ".flow file name", id: 'nameOfFlowFile', row: 1, column: 4, columnSpan: 3, halignment: "left", valignment: 'bottom')

								label("Company ", row: 2, column: 3, columnSpan: 1, textFill: SPIDAGREY, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "center", valignment: 'top')
								textField(promptText: "Company Name", id: 'nameOfCompany', row: 2, column: 4, columnSpan: 3, halignment: "left", valignment: 'top')


								rectangle(width: 2500, height: 1, fill: SPIDAGREY, halignment: 'center', translateY: 10, row: 3, column: 0, columnSpan: 8)

								button("Clean Workspace", id: 'cleanButton',  minWidth: 190, prefWidth: 190, row: 7, column: 1, columnSpan: 2, halignment: "center",
								, onAction: {
									cleanWorkspace(false)
									browserButton.setDisable(true)
									exportButton.setDisable(true)
								})

								label("Gather: ",  textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 5, column: 0, columnSpan: 1, halignment: "right" )
								label("Produce: ",  textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 6, column: 0, columnSpan: 1, halignment: "right" )
								label("Utilities: ", textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 7, column: 0, columnSpan: 1, halignment: "right" )



								button("All Documents", id: 'genDocButton', minWidth: 190, prefWidth: 190, row: 5, column: 5, columnSpan: 2, halignment: "center", onAction: {

									if(  unzipFile(nameOfFlowFile.text) ){

										if ( !( new File('build/Resources/Events/' + nameOfFlowFile.text + '.json') ).exists()  ){


											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureFormsAndUpdateFormTable(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)
											captureActionsAndUpdateActionTable(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)

										} else {
											cleanFirstPopup.show()
											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureFormsAndUpdateFormTable(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)
											captureActionsAndUpdateActionTable(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)
										}
									} else {
										flowPopup.show()
									}
								})

								button("Forms", id: 'genFormsButton',  minWidth: 190, prefWidth: 190, row: 5, column: 3, columnSpan: 2, halignment: "center", onAction: {

									if(  unzipFile(nameOfFlowFile.text) ){

										def fileName = nameOfFlowFile.text + '.json'
										SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
										def companyName = nameOfCompany.text.replaceAll(' ', '~')

										tabPane.getSelectionModel().select(1)
										browserButton.setDisable(false)
										exportButton.setDisable(false)
										cleanButton.setDisable(false)

										captureFormsAndUpdateFormTable(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup)

									}else {
										flowPopup.show()
									}
								})

								button("Actions", id: 'genActionsButton', minWidth: 190, prefWidth: 190, row: 5, column: 1, columnSpan: 2, halignment: "center", onAction: {
										if(  unzipFile(nameOfFlowFile.text) ){

											def fileName = nameOfFlowFile.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(2)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureActionsAndUpdateActionTable(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup)

										}else {
											flowPopup.show()
										}
								})

								button("Create Export Folder", id: 'exportButton',  minWidth: 190, prefWidth: 190, row: 6, column: 1, columnSpan: 2, halignment: "center", onAction: {
									exportWorkflow()
								})

								button("Open in browser", id: 'browserButton',  minWidth: 190, prefWidth: 190, row: 6, column: 3, columnSpan: 2, halignment: "center", onAction: {
									if ( (new File('build/Resources/Events/eventIndex.html')).exists() ) {
										showInBrowser()
									} else {
										noBrowserPopup.show()
									}
								})

								button("Go to Google drive", minWidth: 190, prefWidth: 190, row: 7, column: 3, columnSpan: 2, halignment: "center", onAction: {
									openGoogleDrive()
								})

							}
						}

						tab('Forms', id: 'formTab', closable: false) {
							scrollPane(style: '-fx-background-color: #000000', fitToWidth: true, fitToHeight: true) {
								borderPane(style: '-fx-background-color: #000000'){
									top(align: 'CENTER'){
										label(text: "Forms", style: '-fx-font-size: 24pt; fx-font-weight: bold;-fx-font-family: Verdana;', textFill: WHITE )
									}
									center(align: 'CENTER'){
										gridPane(id: 'formTable', gridLinesVisible: false, hgap: 0, vgap: 0, padding: 0, alignment: "top_center", style: '-fx-background-color: #000000') {
											label(" ", row: 2, column: 0)
										}
									}
								}
							}
						}

						tab('Actions', id: 'actionTab', closable: false) {
							scrollPane(style: '-fx-background-color: #000000', fitToWidth: true, fitToHeight: true) {
								borderPane(style: '-fx-background-color: #000000'){
									top(align: 'CENTER'){
										label(text: "Actions", style: '-fx-font-size: 24pt; fx-font-weight: bold;-fx-font-family: Verdana;', textFill: WHITE )
									}
									center(align: 'CENTER'){
										gridPane(id: 'actionTable', gridLinesVisible: false, hgap: 0, vgap: 0, padding: 0, alignment: "top_center", style: '-fx-background-color: #000000') {
											label(" ", row: 2, column: 0)
										}
									}
								}
							}
						}
		            }
		        }
		    }
			stage(primary: false, id: 'flowPopup', title: "ERROR: Invalid flow file", width: 850, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Invalid .flow file name.  Make sure you downloaded the file and it is in your user home directory/Downloads/ folder", row: 0, column: 0, wrapText: true)
						button("Ok", minWidth: 50, prefWidth: 100, row: 2, column: 0, halignment: "center", onAction: {
							flowPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'companyPopup', title: "ERROR: Invalid company name", width: 600, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Invalid company name.  Check your syntax and try again.", row: 0, column: 0)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							companyPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'noBrowserPopup', title: "ERROR: Cannot show browser", width: 650, height: 150, visible: false) {
				scene {
					gridPane(hgap: 5, vgap: 10, padding: 25, alignment: "top_center" ) {
						label("Cannot open eventIndex.html, Have you generated forms/actions/both?", row: 0, column: 0)
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
							cleanWorkspace(true)
							browserButton.setDisable(true)
							exportButton.setDisable(true)
							cleanFirstPopup.hide()
						})
						button("Nope", minWidth: 100, prefWidth: 100, row: 1, column: 1, halignment: "left", onAction: {
							cleanFirstPopup.hide()
						})
					}
				}
			}
			stage(primary: false, id: 'loginPopup', title: "ERROR: Could not log in to spidasoftware", width: 850, height: 150, visible: false) {
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
