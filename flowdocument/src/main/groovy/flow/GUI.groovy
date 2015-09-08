package flow

import static groovyx.javafx.GroovyFX.*
import groovy.util.AntBuilder
import org.apache.tools.ant.taskdefs.condition.Os
import javafx.collections.*
import groovyx.javafx.SceneGraphBuilder
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.layout.ColumnConstraints
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.shape.Rectangle
import java.net.URLEncoder
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class GUI{

	private static final Color SPIDARED = Color.web("#800000")
	private static final Color SPIDAGREY = Color.web("#7C8180")


	//unzips the .flow file and pulls it into the working directory under build/Resources
	private static boolean unzipFile(flowPath, flowName){

		println "Path: ${flowPath}"
		println "File: ${flowName}"
		def foundFile
		def builder = new AntBuilder()
		new File("build/Resources").mkdir()
		builder.unzip( src: flowPath,  dest: "build/Resources/", overwrite:true )

		File jsonFile = new File('build/Resources/Flow.json')
        jsonFile.renameTo('build/Resources/' + flowName + '.json' )
		builder.delete(dir: 'build/Resources/actions/', failonerror: false)
		builder.delete(dir: 'build/Resources/conditions/', failonerror: false)
		foundFile = true

		return foundFile
	}

	//opens the build/Resources/EventIndex.html page in a local browser
	private static void showInBrowser(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			def scriptDir = new File('build.gradle')
			String sDir = scriptDir.getAbsolutePath() - 'build.gradle'
			println 'file:///' + sDir + 'build/Resources/Events/eventIndex.html'
            ('cmd /c start file:\\\\\\' + sDir + 'build\\Resources\\Events\\eventIndex.html').execute()
        } else {
            def thisDirectory = System.getProperty("user.dir")
            java.awt.Desktop.desktop.browse( ('File://' + thisDirectory + '/build/Resources/Events/eventIndex.html').toURI() )
        }
	}

	//exports the necessary files in the build/Resources folder to be put into google drive
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

	//deletes the build/Resources folder
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

	//opens up the google drive homescreen
	private static void openGoogleDrive(){
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			'cmd /c start https://www.google.com/drive/'.execute()
		} else {
			def thisDirectory = System.getProperty("user.dir")
			java.awt.Desktop.desktop.browse( 'https://www.google.com/drive/'.toURI() )
		}
	}

	//creates an proper alias for a string name given
	private static String makeID(def name){
		def newName = URLEncoder.encode(name)
		newName = newName.replaceAll('\\.', 'p')
		return newName
	}

	//starts a new background thread that calls actiontohtml.js with all the proper action arguments, and then
	//calls generateactionhtml.js on the same thread. Updates the action tab to show a live progress bar and color changes.
	private static void captureActionsAndLiveUpdateActionTab(def mainScene, def sampleFlow, def fileName, def companyName, def actionTable, def companyPopup, def loginPopup, def website){

		def actionNames = sampleFlow.allActions as String[]

		int row = 3
		int rectNum = 0;
		def numOfRects = actionNames.size() * 2 + 4
		def rectWidth = (620 / numOfRects)

		actionNames.each { actionTable.getColumnConstraints().add( new ColumnConstraints(minWidth: rectWidth, prefWidth: rectWidth) ) }

		for (int i = 0; i < numOfRects; i++){
			actionTable.add(new Rectangle( width: rectWidth, id: 'actionRect' + i,  arcWidth: 4, arcHeight: 4, height: 15, fill: SPIDAGREY ), i, 1)
		}

		actionNames.each {action ->
			String actionID = makeID(action)
			actionTable.add(new Label( text: action, id: 'Action' + actionID, style: '-fx-font-weight: bold', textFill: Color.web('#800000'), alignment: 'CENTER' ), 0, row, numOfRects, 1)
			row++
		}

		Closure getActions = {

			println "Starting script"
			def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
			companyName = URLEncoder.encode(companyName, "UTF-8")
			String[] actionArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'src/main/javascript/actiontohtml.js', website, companyName, flowName] as String[]
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
					rectFill?.setFill( Color.web('#007200') )
				}else if (line.contains('Stored screenshot of action: ')) {
					println line
					String tempName = line.replace('Stored screenshot of action: ', '')
					String key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#D48600') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#007200') )

				} else if (line.contains('CasperError: Cannot dispatch mousedown event on nonexistent selector: xpath selector')) {
					println line
					getActions.destroy()
					companyPopup.show()
				} else {
					println line
				}
			}
			createEventDocuments(fileName, companyName)

			String[] genActionArray = ['casperjs', 'src/main/javascript/generateactionhtml.js'] as String[]
			genActionArray = genActionArray + sampleFlow.actionArguments
			def formatActionHtml = genActionArray.execute()
			formatActionHtml.in.eachLine { line ->
				if (line.contains('Created PNG of: ')) {
					println line
					String tempName = line.replace('Created PNG of: ', '')
					String key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#007200') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#007200') )

				} else if (line.contains('Could not find action: ')){
					println line
					String tempName = line.replace('Could not find action: ', '')
					def key = makeID(tempName)

					Label actionStatus = (Label) mainScene.lookup('#' + 'Action' + key)
					actionStatus?.setTextFill( Color.web('#800000') )
					Rectangle rectFill = (Rectangle) mainScene.lookup('#actionRect' + rectNum++)
					rectFill?.setFill( Color.web('#007200') )

				} else {
					println line
				}
			}
		}
		Thread actionThread = new Thread(getActions as Runnable)
		actionThread.setDaemon(true)
		actionThread.start()
	}

	//starts a new background thread that calls formtohtml.js with all the proper action arguments, and then
	//calls generateformhtml.js on the same thread. Updates the form tab to show a live progress bar and color changes.
	private static void captureFormsAndLiveUpdateFormTab(def mainScene, def sampleFlow, def companyName, def formTable, def companyPopup, def loginPopup, def website){
			def formNames = sampleFlow.allForms as String[]

			int row = 3
			int rectNum = 0;
			def numOfRects = formNames.size() * 2 + 5
			def rectWidth = (620 / numOfRects)

			formNames.each { formTable.getColumnConstraints().add( new ColumnConstraints(minWidth: rectWidth, prefWidth: rectWidth) ) }

			for (int i = 0; i < numOfRects; i++){
				formTable.add(new Rectangle( width: rectWidth, id: 'formRect' + i, arcWidth: 4, arcHeight: 4, height: 15, fill: SPIDAGREY ), i, 1)
			}

			formNames.each {form ->
				String formID = makeID(form)
				formTable.add(new Label( text: form, id: 'Form' + formID, style: '-fx-font-weight: bold', textFill: Color.web('#800000'), alignment: 'CENTER' ), 0, row, numOfRects, 1)
				row++
			}


			Closure getForms = {

				println "Starting script"

				def flowName = URLEncoder.encode(sampleFlow.flowName, "UTF-8")
				companyName = URLEncoder.encode(companyName, "UTF-8")
				String[] formArgsArray = ['casperjs', '--ssl-protocol=\"any\"', '--ignore-ssl-errors=true', 'src/main/javascript/formtohtml.js', website, companyName] as String[]
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
						rectFill?.setFill( Color.web('#007200') )
					}
					else if (line.contains('Stored screenshot of form: ')) {
						println line
						String tempName = line.replace('Stored screenshot of form: ', '')
						def key = makeID(tempName)

						Label formStatus = (Label) mainScene.lookup('#' + 'Form' + key)
	                    formStatus?.setTextFill( Color.web('#D48600') )
						Rectangle rectFill = (Rectangle) mainScene.lookup('#formRect' + rectNum++)
						rectFill?.setFill( Color.web('#007200') )

					} else if (line.contains('CasperError: Cannot dispatch mousedown event on nonexistent selector: xpath selector')) {
						companyPopup.show()
						getForms.destroy()
						println line
					} else {
						println line
					}
				}

				String[] genFormArray = ['casperjs', 'src/main/javascript/generateformhtml.js'] as String[]
				genFormArray = genFormArray + sampleFlow.formArguments
				def formatFormHtml = genFormArray.execute()
				formatFormHtml.in.eachLine { line ->
					if (line.contains('Created PNG of: ')) {
						println line
						String tempName = line.replace('Created PNG of: ', '')
						def key = makeID(tempName)
						Label formStatus = (Label) mainScene.lookup('#' + 'Form' + key)
	                    formStatus?.setTextFill( Color.web('#007200') )
						Rectangle rectFill = (Rectangle) mainScene.lookup('#formRect' + rectNum++)
						rectFill?.setFill( Color.web('#007200') )
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

	//creates the Event documentation and puts it inside of build/Resources/Events
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

	//Builds the user interface with three tabs and multiple (currently) invisible popups
	public static void main(args){

		def filePath, fileName

		start {
		    stage(id: 'mainStage', title: "Workflow Document Generator", width: 800, height: 450, visible: true, resizable: false) {

				scene(id: 'mainScene'){

					//add custom stylesheet for UI style overriding
					File css = new File("src/main/groovy/flow/resources/gui.css")
					mainScene.getStylesheets().clear()
					mainScene.getStylesheets().add("file:///" + css.getAbsolutePath().replaceAll('\\\\', "/"))
					def jpg = new File('src/Resources/metallic.jpeg')
					def metal = 'file:///' + jpg.getAbsolutePath().replaceAll("\\\\", "/")

					tabPane (id: 'tabPane', style: '-fx-background-color: #000000;-fx-border-color: #7C8180;-fx-border-width: 0px 1px 1px 1px;') {

						//Straightforward tab for basic user
						tab('Main', id: 'mainTab', closable: false) {
							gridPane(id: 'gridPane', hgap: 5, vgap: 10, padding: 0, gridLinesVisible: false, alignment: "top_center", style: "-fx-background-image: url(${metal}); -fx-background-repeat: stretch;-fx-background-size: 800 450") {
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')
								columnConstraints(minWidth: 100, prefWidth: 100, hgrow: 'never')

								imageView(row: 0, column: 0, columnSpan: 2, rowSpan: 4, fitWidth: 275, preserveRatio: true){
									def img = new File('src/Resources/SpidaLogo.png')
									image('file:///' + img.getAbsolutePath().replace("\\", "/"))
								}

								label("Website URL", row: 1, column: 3, columnSpan: 1, textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "left", valignment: 'center')
								textField(promptText: "Website", id: 'website', row: 1, column: 4, columnSpan: 3, halignment: "left", valignment: 'center', onAction: {
										String url = website.text
										def dotComIndex = url.indexOf('.com')
										if (dotComIndex > 0){
											website.text = url.substring(0, dotComIndex + 4)
										}
								})

								label("Company ", row: 2, column: 3, columnSpan: 1, textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "center", valignment: 'center')
								textField(promptText: "Company Name", id: 'nameOfCompany', row: 2, column: 4, columnSpan: 3, halignment: "left", valignment: 'center')

								label("Flow File ", row: 3, column: 3, columnSpan: 1, rowSpan: 2, textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "center", valignment: 'center')

								rectangle(fill: Color.TRANSPARENT, width: 800, height: 100, row: 6, column: 0, rowSpan: 2, columnSpan: 8)
								final flowFile = rectangle(fill: WHITE, width: 307, height: 80, row: 3, column: 4, rowSpan: 2, opacity: 0.4, columnSpan: 3, arcHeight: 5, arcWidth: 5)
								final flowText = text("drag .flow file here", row: 3, column: 4, columnSpan: 3, rowSpan: 2, halignment: 'center' )


								flowFile.setOnDragOver(new EventHandler<DragEvent>() {
						            @Override
						            public void handle(DragEvent event) {
										Dragboard db = event.getDragboard()
						                if (db.hasFiles()) {
						                    event.acceptTransferModes(TransferMode.ANY)
											flowFile.opacity = 1.0
						                } else {
						                    event.consume()
						                }
						            }
						    	})

								flowFile.setOnDragEntered(new EventHandler <DragEvent>() {
						            public void handle(DragEvent event) {
						                flowFile.opacity = 1.0
						                event.consume()
						            }
						        })

								flowFile.setOnDragExited(new EventHandler <DragEvent>() {
							        public void handle(DragEvent event) {
							            flowFile.opacity = 0.5
							            event.consume()
							        }
							    })

								flowFile.setOnDragDropped(new EventHandler<DragEvent>() {
									@Override
									public void handle(DragEvent event) {
										Dragboard db = event.getDragboard()
										boolean success = false
										if (db.hasFiles()) {
											success = true
											File file = db.getFiles()[0]
											filePath = file.getAbsolutePath()
											fileName = file.name - '.flow'
											flowFile.opacity = 1.0
											flowText.setText(fileName)
										}
										event.setDropCompleted(success)
										event.consume()
									}
								})

								flowText.setOnDragOver(new EventHandler<DragEvent>() {
						            @Override
						            public void handle(DragEvent event) {
										Dragboard db = event.getDragboard()
						                if (db.hasFiles()) {
						                    event.acceptTransferModes(TransferMode.ANY)
											flowFile.opacity = 1.0
						                } else {
						                    event.consume()
						                }
						            }
						    	})

								flowText.setOnDragEntered(new EventHandler <DragEvent>() {
									@Override
									public void handle(DragEvent event) {
						                flowFile.opacity = 1.0
						                event.consume()
						            }
						        })

								flowText.setOnDragExited(new EventHandler <DragEvent>() {
									@Override
									public void handle(DragEvent event) {
							            flowFile.opacity = 0.5
							            event.consume()
							        }
							    })

								flowText.setOnDragDropped(new EventHandler<DragEvent>() {
									@Override
									public void handle(DragEvent event) {
										Dragboard db = event.getDragboard()
										boolean success = false
										if (db.hasFiles()) {
											success = true
											File file = db.getFiles()[0]
											filePath = file.getAbsolutePath()
											fileName = file.name - '.flow'
											flowFile.opacity = 1.0
											flowText.setText(fileName)
										}
										event.setDropCompleted(success)
										event.consume()
									}
								})

								button("Generate documents", id: 'genDocButton', minWidth: 250, prefWidth: 250, minHeight: 75, row: 7, column: 0, , rowSpan: 2, columnSpan: 4, valignment: "bottom", halignment: "center", onAction: {

									String url = website.text
									def dotComIndex = url.indexOf('.com')
									if (dotComIndex > 0){
										website.text = url.substring(0, dotComIndex + 4)
									}

									cleanWorkspace()
									try {
										println unzipFile(filePath, fileName)
									} catch (FileNotFoundException e){
										println "Error: ${e}"
									}

									fileName = fileName + '.json'
									SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
									def companyName = nameOfCompany.text.replaceAll(' ', '~')

									tabPane.getSelectionModel().select(2)

									captureActionsAndLiveUpdateActionTab(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup, website.text)
									captureFormsAndLiveUpdateFormTab(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup, website.text)
								})

								button("Export Results", id: 'expResultsButton', minWidth: 250, prefWidth: 250, minHeight: 75, row: 7, column: 3, columnSpan: 4, rowSpan: 2, valignment: "bottom", halignment: "center", onAction: {

									exportWorkflow()
									showInBrowser()
									openGoogleDrive()

								})

							}
						}

						//Advanced operations tab in the user interface
						tab('Advanced', id: 'advancedTab', closable: false) {
		                    gridPane(id: 'gridPane', hgap: 5, vgap: 10, padding: 0, alignment: "top_center", style: "-fx-background-image: url(${metal}); -fx-background-repeat: stretch;-fx-background-size: 800 450") {
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

								imageView(row: 0, column: 0, columnSpan: 2, rowSpan: 4, fitWidth: 275, preserveRatio: true){
									def img = new File('src/Resources/SpidaLogo.png')
									image('file:///' + img.getAbsolutePath().replace("\\", "/"))
								}

								label("Flow Name ", id: 'flowNameField', hgrow: "never", style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 1, column: 3, columnSpan: 1, valignment: 'center', halignment: "center", textFill: WHITE)
								textField(promptText: ".flow file name", id: 'nameOfFlowFile2', row: 1, column: 4, columnSpan: 3, halignment: "left", valignment: 'center')

								label("Company ", row: 2, column: 3, columnSpan: 1, textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "center", valignment: 'center')
								textField(promptText: "Company Name", id: 'nameOfCompany2', row: 2, column: 4, columnSpan: 3, halignment: "left", valignment: 'center')

								label("Website URL", row: 3, column: 3, columnSpan: 1, textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', halignment: "left", valignment: 'center')
								textField(promptText: "Website", id: 'website2', row: 3, column: 4, columnSpan: 3, halignment: "left", valignment: 'center', onAction: {
										String url = website2.text
										def dotComIndex = url.indexOf('.com')
										if (dotComIndex > 0){
											website2.text = url.substring(0, dotComIndex + 4)
										}
								})

								button("Clean Workspace", id: 'cleanButton',  minWidth: 190, prefWidth: 190, row: 8, column: 1, columnSpan: 2, halignment: "center",
								, onAction: {
									cleanWorkspace(false)
									browserButton.setDisable(true)
									exportButton.setDisable(true)
								})

								label("Gather: ",  textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 6, column: 0, columnSpan: 1, halignment: "right" )
								label("Produce: ",  textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 7, column: 0, columnSpan: 1, halignment: "right" )
								label("Utilities: ", textFill: WHITE, style: '-fx-font-size: 15;-fx-font-family: Verdana;', row: 8, column: 0, columnSpan: 1, halignment: "right" )

								button("All Documents", id: 'genDocButton', minWidth: 190, prefWidth: 190, row: 6, column: 5, columnSpan: 2, halignment: "center", onAction: {

									String url = website2.text
									def dotComIndex = url.indexOf('.com')
									if (dotComIndex > 0){
										website2.text = url.substring(0, dotComIndex + 4)
									}

									if(  unzipFile(nameOfFlowFile2.text) ){

										if ( !( new File('build/Resources/Events/' + nameOfFlowFile2.text + '.json') ).exists()  ){
											fileName = nameOfFlowFile2.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany2.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureFormsAndLiveUpdateFormTab(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup, website2.text)
											captureActionsAndLiveUpdateActionTab(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup, website2.text)

										} else {
											cleanFirstPopup.show()
											fileName = nameOfFlowFile2.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany2.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(1)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureFormsAndLiveUpdateFormTab(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup, website2.text)
											captureActionsAndLiveUpdateActionTab(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup, website2.text)
										}
									} else {
										flowPopup.show()
									}
								})

								button("Forms", id: 'genFormsButton',  minWidth: 190, prefWidth: 190, row: 6, column: 3, columnSpan: 2, halignment: "center", onAction: {

									String url = website2.text
									def dotComIndex = url.indexOf('.com')
									if (dotComIndex > 0){
										website2.text = url.substring(0, dotComIndex + 4)
									}

									if(  unzipFile(nameOfFlowFile2.text) ){
										fileName = nameOfFlowFile2.text + '.json'
										SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
										def companyName = nameOfCompany2.text.replaceAll(' ', '~')

										tabPane.getSelectionModel().select(1)
										browserButton.setDisable(false)
										exportButton.setDisable(false)
										cleanButton.setDisable(false)

										captureFormsAndLiveUpdateFormTab(mainScene, sampleFlow, companyName, formTable, companyPopup, loginPopup, website2.text)

									}else {
										flowPopup.show()
									}
								})

								button("Actions", id: 'genActionsButton', minWidth: 190, prefWidth: 190, row: 6, column: 1, columnSpan: 2, halignment: "center", onAction: {
										String url = website2.text
										def dotComIndex = url.indexOf('.com')
										if (dotComIndex > 0){
											website2.text = url.substring(0, dotComIndex + 4)
										}


										if(  unzipFile(nameOfFlowFile2.text) ){

											fileName = nameOfFlowFile2.text + '.json'
											SampleWorkFlow sampleFlow = new SampleWorkFlow(fileName)
											def companyName = nameOfCompany2.text.replaceAll(' ', '~')

											tabPane.getSelectionModel().select(2)
											browserButton.setDisable(false)
											exportButton.setDisable(false)
											cleanButton.setDisable(false)

											captureActionsAndLiveUpdateActionTab(mainScene, sampleFlow, fileName, companyName, actionTable, companyPopup, loginPopup, website.text)

										}else {
											flowPopup.show()
										}
								})

								button("Create Export Folder", id: 'exportButton',  minWidth: 190, prefWidth: 190, row: 7, column: 1, columnSpan: 2, halignment: "center", onAction: {
									exportWorkflow()
								})

								button("Open in browser", id: 'browserButton',  minWidth: 190, prefWidth: 190, row: 7, column: 3, columnSpan: 2, halignment: "center", onAction: {
									if ( (new File('build/Resources/Events/eventIndex.html')).exists() ) {
										showInBrowser()
									} else {
										noBrowserPopup.show()
									}
								})

								button("Go to Google drive", minWidth: 190, prefWidth: 190, row: 8, column: 3, columnSpan: 2, halignment: "center", onAction: {
									openGoogleDrive()
								})
							}
						}

						//The form tab definition
						tab('Forms', id: 'formTab', closable: false) {
							scrollPane(fitToWidth: true, fitToHeight: true) {
								borderPane(style: "-fx-background-color: #252525;"){
									top(align: 'CENTER'){
										label(text: "Forms", style: '-fx-font-size: 24pt; fx-font-weight: bold;-fx-font-family: Verdana;', textFill: WHITE )
									}
									center(align: 'CENTER'){
										gridPane(id: 'formTable', gridLinesVisible: false, hgap: 0, vgap: 0, padding: 0, alignment: "top_center") {
											label(" ", row: 2, column: 0)
										}
									}
								}
							}
						}

						//The action tab definition
						tab('Actions', id: 'actionTab', closable: false) {
							scrollPane(fitToWidth: true, fitToHeight: true) {
								borderPane(style: "-fx-background-color: #252525;"){
									top(align: 'CENTER'){
										label(text: "Actions", style: '-fx-font-size: 24pt; fx-font-weight: bold;-fx-font-family: Verdana;', textFill: WHITE )
									}
									center(align: 'CENTER'){
										gridPane(id: 'actionTable', gridLinesVisible: false, hgap: 0, vgap: 0, padding: 0, alignment: "top_center") {
											label(" ", row: 2, column: 0)
										}
									}
								}
							}
						}
		            }
		        }
		    }

			//popup for an invalid flow file name
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

			//popup for an invalid company name
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

			//popup for a missing eventIndex.html file when pressing the "Open in Browser" button
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

			//popup asking if the user forgot to clean the working directory before running a new flow
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

			//popup telling the user that there was an error logging into the website
			stage(primary: false, id: 'loginPopup', title: "ERROR: Could not log in to this website", width: 850, height: 150, visible: false) {
				scene {
					gridPane(hgap: 10, vgap: 10, padding: 25, alignment: "top_center" ) {
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						columnConstraints(minWidth: 250, prefWidth: 250, hgrow: 'never')
						label("Error trying to log into https://demo.spidastudio.com/projectmanager/\nCheck internet connection and try again.", halignment: 'center', row: 0, column: 0, columnSpan: 2)
						button("Ok", minWidth: 50, prefWidth: 100, row: 1, column: 0, halignment: "center", onAction: {
							loginPopup.hide()
						})
					}
				}
			}
		}
	}
}
