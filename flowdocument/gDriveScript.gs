//GLOBAL AND CHANGEABLE STYLES FOR DOCUMENT ITEMS
var FONT_CHOICE = "TIMES NEW ROMAN";
var FONT_SIZE = 11;
var SPIDA_RED = '#800000';
var colon = /[colon]/;
var quote = /\"/;

//ALL LIST STYLES
var LIST_STYLE = {};
LIST_STYLE[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
LIST_STYLE[DocumentApp.Attribute.FONT_SIZE] = 11;
LIST_STYLE[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
LIST_STYLE[DocumentApp.Attribute.PADDING_TOP] = 0;
LIST_STYLE[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
LIST_STYLE[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
LIST_STYLE[DocumentApp.Attribute.MARGIN_TOP] = 0;
LIST_STYLE[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
LIST_STYLE[DocumentApp.Attribute.BOLD] = false;
LIST_STYLE[DocumentApp.Attribute.UNDERLINE] = false;
LIST_STYLE[DocumentApp.Attribute.ITALIC] = false;
LIST_STYLE[DocumentApp.Attribute.GLYPH_TYPE] = DocumentApp.GlyphType.HOLLOW_BULLET; //SQUARE_BULLET or HOLLOW_BULLET or NUMBER

//Normal body document text style
var PLAIN_TEXT = {};
PLAIN_TEXT[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
PLAIN_TEXT[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
PLAIN_TEXT[DocumentApp.Attribute.FONT_SIZE] = 11;
PLAIN_TEXT[DocumentApp.Attribute.BOLD] = false;
PLAIN_TEXT[DocumentApp.Attribute.UNDERLINE] = false;
PLAIN_TEXT[DocumentApp.Attribute.PADDING_TOP] = 0;
PLAIN_TEXT[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
PLAIN_TEXT[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
PLAIN_TEXT[DocumentApp.Attribute.MARGIN_TOP] = 0;
PLAIN_TEXT[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
PLAIN_TEXT[DocumentApp.Attribute.ITALIC] = false;

//Action/form/event page header styles
var HEADER_STYLE = {};
HEADER_STYLE[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
HEADER_STYLE[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
HEADER_STYLE[DocumentApp.Attribute.FONT_SIZE] = 16;
HEADER_STYLE[DocumentApp.Attribute.BOLD] = false;
HEADER_STYLE[DocumentApp.Attribute.UNDERLINE] = false;
HEADER_STYLE[DocumentApp.Attribute.PADDING_TOP] = 0;
HEADER_STYLE[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
HEADER_STYLE[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
HEADER_STYLE[DocumentApp.Attribute.MARGIN_TOP] = 0;
HEADER_STYLE[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
HEADER_STYLE[DocumentApp.Attribute.ITALIC] = true;


//**********************************************Beginning/utility functions************************************

//creates the menu to generate the various output pages for the workflow
function onOpen(e) {

  var ui = DocumentApp.getUi();
  ui.createMenu('Create Workflow Output').addItem('Generate Form Document', 'generateFormDocument').addSeparator()
  .addItem('Generate Action Document', 'generateActionDocument').addSeparator()
  .addItem('Generate Event Document', 'generateEventDocument').addToUi();

};


//prompts the user for the name of the workflow they wish to generate the output for
function getFlowName(){

  var ui = DocumentApp.getUi();
  var folderName = ui.prompt('What is the name of the workflow folder, inside of \"My Drive\"?', ui.ButtonSet.OK);

  if (folderName.getSelectedButton() == ui.Button.OK) {
    var workFlowName = folderName.getResponseText();
  };
  return workFlowName;

};


//prompts the user for the name of the company this flow is for
function getCompanyAndFlowNames(resourceFolderChild){

  var parents = resourceFolderChild.getParents();
  var flowInfo;

  while (parents.hasNext()){
    var resourcesFolder = parents.next();
    if (resourcesFolder.getName() === "Resources"){
      var resourceIterate = resourcesFolder.getFilesByName("flowInfo.txt");
      while (resourceIterate.hasNext()){
        flowInfo = resourceIterate.next();
      };
    };
  };

  var infoArray = [];
  var info = flowInfo.getBlob().getDataAsString();
  infoArray = info.split('\n');

 return infoArray;

};


//Sets the workflow folder permissions to public, so the images can be referenced by url later in the script
function setPermissions(workFlowName){

  var workFlowFolder = DriveApp.getFoldersByName(workFlowName);
  while (workFlowFolder.hasNext()){
    workFlowFolder.next().setSharing(DriveApp.Access.ANYONE, DriveApp.Permission.EDIT);
  };
};




//*************************************************ADD-ON MENU FUNCTIONS**********************************************//

//Creates the Form document
function generateFormDocument(){

  var workFlowName = getFlowName();
  setPermissions(workFlowName);
  var formHtmls = getHtmlFolder(workFlowName, "form");
  var formMap = createHtmlMap(formHtmls);
  var imageMap = {};
  var flowAndCompany = getCompanyAndFlowNames(formHtmls)

  formMap = alphabetizeMap(formMap);
  replaceImageSource(formMap, imageMap, formHtmls, "form");
  createCoverPage(formHtmls, "Forms", flowAndCompany);
  createCopyrightPage();
  createIndexPage(formMap, "Form");
  createBody(formMap, imageMap, "form");
  putFooters(flowAndCompany[1]);
};


//Creates the Action document
function generateActionDocument(){

  var workFlowName = getFlowName();
  setPermissions(workFlowName);
  var actionHtmls = getHtmlFolder(workFlowName, 'action');
  var actionMap = createHtmlMap(actionHtmls);
  var imageMap = {};
  var flowAndCompany = getCompanyAndFlowNames(actionHtmls);

  actionMap = alphabetizeMap(actionMap);
  replaceImageSource(actionMap, imageMap, actionHtmls, 'action');
  createCoverPage(actionHtmls, "Actions", flowAndCompany);
  createCopyrightPage();
  createIndexPage(actionMap, "Action");
  createBody(actionMap, imageMap, "action");
  putFooters(flowAndCompany[1]);
};


//creates the Events document
function generateEventDocument(){

  var workFlowName = getFlowName();
  setPermissions(workFlowName);
  var eventHtmls = getHtmlFolder(workFlowName, 'Events');
  var eventTexts = getEventTexts(eventHtmls);
  var eventOrder = getEventOrder(eventHtmls);
  var flowAndCompany = getCompanyAndFlowNames(eventHtmls);

  createCoverPage(eventHtmls, "Events", flowAndCompany);
  createCopyrightPage();
  createEventIndexPage(eventOrder);
  createEventPage(eventTexts, eventOrder);
  putFooters(flowAndCompany[1]);
};



//*****************************************Multi-purpose Functions*************************************************//

//formats the footer on all pages in the document.  CANNOT ADD PAGE #
function putFooters(company){

  var doc = DocumentApp.getActiveDocument();
  var footer = doc.getFooter();
  var hr;

  if (footer !== null){
    footer.clear();
  } else{
    footer = doc.addFooter();
  };

  hr = footer.appendParagraph('');

  var hrStyle = {};
  hrStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  hrStyle[DocumentApp.Attribute.FONT_SIZE] = 14;
  hrStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  hrStyle[DocumentApp.Attribute.UNDERLINE] = false;

  hr.setAttributes(hrStyle);

  hr.appendText('⎼').setAttributes(hrStyle);
  hr.appendText('⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼⎼').setAttributes(hrStyle).setUnderline(true);
  hr.appendText('⎼').setAttributes(hrStyle);

  var footerText = footer.appendParagraph("© SPIDAWeb LLC ◆ CONFIDENTIAL ◆ " + company + " ONLY\n");

  var footerStyle = {};
  footerStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  footerStyle[DocumentApp.Attribute.FONT_SIZE] = FONT_SIZE;
  footerStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  footerStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
  footerStyle[DocumentApp.Attribute.UNDERLINE] = false;

  footerText.setAttributes(footerStyle);

  footer.appendParagraph("PAGE ").setAttributes(footerStyle);
};


//Returns MyDrive/workFlowName/Resources/formHtmls (class Folder)
function getHtmlFolder(workFlowName, type) {

  var myDrive = DriveApp.getFoldersByName(workFlowName);
  var workFlowFolder;
  var resourcesFolder;
  var htmls;

  while (myDrive.hasNext()){
    workFlowFolder = myDrive.next();
    workFlowFolder = workFlowFolder.getFoldersByName("Resources");
    while (workFlowFolder.hasNext()){
      resourcesFolder = workFlowFolder.next();
      if (type === 'Events'){
      resourcesFolder = resourcesFolder.getFoldersByName(type);
      } else {
        resourcesFolder = resourcesFolder.getFoldersByName(type + 'Htmls');
      };
      while (resourcesFolder.hasNext()){
        htmls = resourcesFolder.next();
      };
    };
  };
  return htmls;
};

//creates and returns a map of {"form/action name" : "html text"} pairs
function createHtmlMap(folder){
  var htmlMap = {};
  var files = folder.getFiles();

  while (files.hasNext()) {
    var file = files.next();
    var fileName = file.getName();
    var newFileName = fileName.replace('.html','');

    if (fileName !== newFileName){
      htmlMap[newFileName] = file.getBlob().getDataAsString();
    };
  };
  return  htmlMap;
};


//Replaces all of the urls in the src="url" links in the map so they actually work inside Google Drive
function replaceImageSource(map, imageMap, folder, type){

  var pngs = folder.getFoldersByName(type + 'PNGs');
  var formPNGs;

  while (pngs.hasNext()) {
    formPNGs = pngs.next();
  };
  pngs = formPNGs.getFiles();

  while (pngs.hasNext()){
    var png = pngs.next();
    var pngName = png.getName();
    pngName = pngName.replace('.png', '');

    var id = png.getId();
    var html = map[pngName];

    if (html !== undefined){
      map[pngName] = createList(html);
      imageMap[pngName] = 'http://drive.google.com/uc?export=view&id=' + id;
    };
  };
};

//returns the list of field values for the given action/form html element
function createList(html){

  var list = [];
  while (html.indexOf('<li>') != -1){

    list.push(html.substring(html.indexOf('<li>') + 4, html.indexOf('</li>')));
    html = html.replace('<li>', '');
    html = html.replace('</li>', '');
  };

  return list;
};


//inserts all of the form/action pages into the document
function createBody(map, imageMap, type) {

  var body = DocumentApp.getActiveDocument().getBody();
  var text = body.editAsText();

  for (var item in map){
    putHeader(item, body, type);
    putScreenshot(imageMap[item],body);
    putFieldValues(map[item], body);
  };
};


//creates a cover page with the spida emblem and an approriate document title
function createCoverPage(htmlsFolder, type, companyAndFlow){

  var company = companyAndFlow[1];
  var flowName = companyAndFlow[0];

  var date = new Date();
  var day = date.getDate();
  var month = date.getMonth()+1;
  var year = date.getFullYear();

  var today = month + '/' + day + '/' + year;

  var parents = htmlsFolder.getParents();
  var spidaImageUrl;

  while (parents.hasNext()){
    var resourcesFolder = parents.next();
    if (resourcesFolder.getName() === "Resources"){
      var resourceIterate = resourcesFolder.getFilesByName("SpidaEmblem.png");
      while (resourceIterate.hasNext()){
        var spidaPNG = resourceIterate.next();
        spidaImageUrl = 'http://drive.google.com/uc?export=view&id=' + spidaPNG.getId();
      };
    };
  };

  var body = DocumentApp.getActiveDocument().getBody();

  var headerStyle = {};
  headerStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  headerStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  headerStyle[DocumentApp.Attribute.FONT_SIZE] = FONT_SIZE;
  headerStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;

  var titleStyle = {};
  titleStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  titleStyle[DocumentApp.Attribute.FONT_SIZE] = 30;
  titleStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  titleStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  titleStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  titleStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  titleStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  titleStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;


  var subStyle = {};
  subStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  subStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  subStyle[DocumentApp.Attribute.FONT_SIZE] = 18;
  subStyle[DocumentApp.Attribute.BOLD] = true;
  subStyle[DocumentApp.Attribute.UNDERLINE] = false;
  subStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  subStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  subStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  subStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  subStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
  subStyle[DocumentApp.Attribute.ITALIC] = true;

  var dateStyle = {};
  dateStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  dateStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  dateStyle[DocumentApp.Attribute.UNDERLINE] = false;
  dateStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  dateStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  dateStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  dateStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  dateStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
  dateStyle[DocumentApp.Attribute.FONT_SIZE] = 14;
  dateStyle[DocumentApp.Attribute.BOLD] = false;
  dateStyle[DocumentApp.Attribute.ITALIC] = false;

  var img = UrlFetchApp.fetch(spidaImageUrl);
  body.appendParagraph(flowName).setAttributes(titleStyle);
  body.appendImage(img.getBlob());
  body.appendParagraph('_____________________________________________________________\n').setAttributes(headerStyle);
  body.appendParagraph("SPIDAMin: " + type ).setAttributes(titleStyle);
  body.appendParagraph('_____________________________________________________________').setAttributes(headerStyle);
  body.appendParagraph('\nPrepared for ' + company).setAttributes(subStyle);
  body.appendParagraph('\n' + today).setAttributes(dateStyle);

};



//creates a copyright page
function createCopyrightPage(){

  var body = DocumentApp.getActiveDocument().getBody();
  var year = new Date();
  year = year.getFullYear();


  body.appendPageBreak();
  var infoStyle = {};
  infoStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  infoStyle[DocumentApp.Attribute.FONT_SIZE] = 11;
  infoStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  infoStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  infoStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  infoStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  infoStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  infoStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  infoStyle[DocumentApp.Attribute.BOLD] = false;
  infoStyle[DocumentApp.Attribute.UNDERLINE] = false;
  infoStyle[DocumentApp.Attribute.ITALIC] = false;

  var formalStyle = {};
  formalStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
  formalStyle[DocumentApp.Attribute.FONT_SIZE] = 11;
  formalStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  formalStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  formalStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  formalStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  formalStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  formalStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
  formalStyle[DocumentApp.Attribute.BOLD] = true;
  formalStyle[DocumentApp.Attribute.UNDERLINE] = false;
  formalStyle[DocumentApp.Attribute.ITALIC] = false;



  body.appendParagraph('\n\n\n\n\n');
  var infoText = body.appendParagraph('______________________________________________\n\nSPIDAWEB LLC\n' +
                                      '\n560 OFFICENTER PL, GAHANNA, OH 43230\n\n(614) 470-9882\n\nWWW.SPIDASOFTWARE.COM' +
                                      '\n______________________________________________');

  infoText.setAttributes(infoStyle);

  body.appendParagraph('\n\n\n\n\n\n\n');
  var proprietaryText = body.appendParagraph('Proprietary Information: ').setAttributes(formalStyle);
  proprietaryText.setAttributes(formalStyle);
  proprietaryText.appendText('The information contained in this document is the property of ' +
                             'SPIDAWeb LLC and is furnished to the recipient as confidential matter. The holder of this ' +
                             'document shall not share, disclose, divulge, or otherwise communicate the document’s contents, in ' +
                             'whole or in part, to any third party except as expressly authorized by SPIDAWeb LLC.\n').setBold(false);

  var copyrightText = body.appendParagraph('Copyright: ').setAttributes(formalStyle);
  copyrightText.setAttributes(formalStyle);
  copyrightText.appendText('© ' + year + ' SPIDAWeb LLC. All rights reserved. No part of this document may be ' +
                           'reproduced, transmitted, transcribed, or translated into any language without the prior written ' +
                           'permission of SPIDAWeb LLC.\n').setBold(false);

  var spidaText = body.appendParagraph('SPIDA® ').setAttributes(formalStyle);
  spidaText.setAttributes(formalStyle);
  spidaText.appendText('is a registered trademark of SPIDAWeb LLC. All other brands or product names are the ' +
                       'property of their respective holders.').setBold(false);

};



//creates the action/form index page with formatted listings of all items in each category.  (No specific order)
function createIndexPage(map, type){

  var body = DocumentApp.getActiveDocument().getBody();

  var headerStyle = {};
  headerStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
  headerStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  headerStyle[DocumentApp.Attribute.FONT_SIZE] = 30;
  headerStyle[DocumentApp.Attribute.BOLD] = false;
  headerStyle[DocumentApp.Attribute.UNDERLINE] = false;
  headerStyle[DocumentApp.Attribute.ITALIC] = false;
  headerStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  headerStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  headerStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;

  body.appendPageBreak();
  var indexHeader = body.appendParagraph(type + " Index");
  indexHeader.setAttributes(headerStyle);

  body.appendHorizontalRule();

  for (var item in map){
    var listElement = body.appendListItem(item)
    listElement.setAttributes(LIST_STYLE).setItalic(true);
  };
};


//creates a header at the top of each action/form page with the name of the action/form
function putHeader(name, body, type){

  name = name.replace(colon, ':');
  body.appendPageBreak();
  var header
  var topDashedLine = body.appendParagraph('---------------------------------------------------------------------------------------------------------------------');
  if (type === 'event'){
    header = body.appendParagraph(name);
  } else{
    header = body.appendParagraph(type.toUpperCase() + ": " + name.toUpperCase());
  };

  var headerStyle = {};
  headerStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
  headerStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  headerStyle[DocumentApp.Attribute.FONT_SIZE] = FONT_SIZE;
  headerStyle[DocumentApp.Attribute.BOLD] = false;
  headerStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  headerStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  headerStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  headerStyle[DocumentApp.Attribute.UNDERLINE] = false;
  headerStyle[DocumentApp.Attribute.ITALIC] = false;

  var bottomDashedLine = body.appendParagraph('---------------------------------------------------------------------------------------------------------------------');

  header.setAttributes(headerStyle);
  bottomDashedLine.setAttributes(headerStyle);
  topDashedLine.setAttributes(headerStyle);
};


//places the screenshot of the action/form inside the document
function putScreenshot(url,body){

  var image = UrlFetchApp.fetch(url);

  var name = image.getBlob().getName();
  Logger.log(name);

  if (name === 'Untitled'){
    Logger.log('missed an image')
    body.appendParagraph('Missing image, sorry.');

  }else {

    var imgWrapper = body.appendParagraph("");
    var img = imgWrapper.appendInlineImage(image.getBlob())

    var height = img.getHeight()
    if (img.getHeight() > 700){
      img.setHeight(700);
    };

    var imageStyle = {};
    imageStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
    imageStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
    imageStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
    imageStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
    imageStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
    imageStyle[DocumentApp.Attribute.UNDERLINE] = false;
    imageStyle[DocumentApp.Attribute.ITALIC] = false;

    imgWrapper.setAttributes(imageStyle);
  };
};


//creates the bulleted list of field values inside of the form
function putFieldValues(fields, body){

  if (typeof fields !== 'string'){

    var heading = body.appendParagraph("Field values and description:");
    var headingStyle = {};
    headingStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
    headingStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
    headingStyle[DocumentApp.Attribute.FONT_SIZE] = FONT_SIZE;
    headingStyle[DocumentApp.Attribute.BOLD] = true;
    headingStyle[DocumentApp.Attribute.UNDERLINE] = true;
    headingStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
    headingStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
    headingStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
    headingStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
    headingStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = '#000000';
    headingStyle[DocumentApp.Attribute.ITALIC] = false;

    heading.setAttributes(headingStyle);

    for (var i = 0; i < fields.length; i++){
      var listElement = body.appendListItem(fields[i]);
      listElement.setAttributes(LIST_STYLE);
    };

  };
};


//alphabetizes and returns the map
function alphabetizeMap(map){

  var newMap = {};
  var sortedArray = [], i = 0;
  for(var item in map){
    sortedArray[i] = item;
    i++;
  };

  for (var x = sortedArray.length - 1 ; x > 0 ; x--){
    newMap[sortedArray[x]] = map[sortedArray[x]];
  };

  return newMap;

};





//*****************************************EVENT FUNCTIONS********************************************//
//----------------------------------------------------------------------------------------------------//

//returns the Resources/Events/texts folder
function getEventTexts(eventHtmls){

  var eventTexts = eventHtmls.getFoldersByName('texts');
  var texts;

  while (eventTexts.hasNext()){
    texts = eventTexts.next();
  };

  return texts;
};


//**********************************************createEventIndexPage()************************************
function createEventIndexPage(eventOrder){

  var body = DocumentApp.getActiveDocument().getBody();
  var headerStyle = {};
  headerStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.LEFT;
  headerStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  headerStyle[DocumentApp.Attribute.FONT_SIZE] = 30;
  headerStyle[DocumentApp.Attribute.BOLD] = false;
  headerStyle[DocumentApp.Attribute.UNDERLINE] = false;
  headerStyle[DocumentApp.Attribute.ITALIC] = false;
  headerStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  headerStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  headerStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  headerStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;

  body.appendPageBreak();

  var indexHeader = body.appendParagraph('Event Index');
  indexHeader.setAttributes(headerStyle);

  body.appendHorizontalRule();

  for (var i=0; i < eventOrder.length; i++){
    var listElement = body.appendListItem(eventOrder[i])
    listElement.setAttributes(LIST_STYLE).setItalic(true);
  };
};


//**********************************************createEventPage()***************************************
function createEventPage(eventTexts, eventOrder){
  var eventIterator = eventTexts.getFiles();
  var events;

  var body = DocumentApp.getActiveDocument().getBody();
  var dataArray = new Array();

  var dataMap = {};
  while (eventIterator.hasNext()){
    var file = eventIterator.next();
    var fileName = file.getName().replace('.txt','');
    var data = file.getBlob().getDataAsString();
    dataArray = data.split('\n');
    dataMap[fileName] = dataArray;
  };

  for (var i = 0; i < eventOrder.length; i++){
    Logger.log(eventOrder[i].toString());

    body.appendPageBreak();
    putEventHeader(dataMap[eventOrder[i]], body);
    putEventInfo(dataMap[eventOrder[i]], body);
    putActionInfo(dataMap[eventOrder[i]], body);
    putFormInfo(dataMap[eventOrder[i]], body);
    putEventDescription(body);
  };

};




//**********************************************putEventHeader()***************************************
function putEventHeader(dataArray, body){

  Logger.log (typeof dataArray);
  Logger.log(dataArray.toString());

  var titleStyle = {};
  titleStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  titleStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  titleStyle[DocumentApp.Attribute.FONT_SIZE] = 16;
  titleStyle[DocumentApp.Attribute.BOLD] = true;
  titleStyle[DocumentApp.Attribute.UNDERLINE] = false;
  titleStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  titleStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  titleStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  titleStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  titleStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  titleStyle[DocumentApp.Attribute.ITALIC] = false;

  var lineStyle = {};
  lineStyle[DocumentApp.Attribute.HORIZONTAL_ALIGNMENT] = DocumentApp.HorizontalAlignment.CENTER;
  lineStyle[DocumentApp.Attribute.FONT_FAMILY] = FONT_CHOICE;
  lineStyle[DocumentApp.Attribute.FONT_SIZE] = FONT_SIZE;
  lineStyle[DocumentApp.Attribute.BOLD] = true;
  lineStyle[DocumentApp.Attribute.UNDERLINE] = true;
  lineStyle[DocumentApp.Attribute.PADDING_TOP] = 0;
  lineStyle[DocumentApp.Attribute.PADDING_BOTTOM] = 0;
  lineStyle[DocumentApp.Attribute.MARGIN_BOTTOM] = 0;
  lineStyle[DocumentApp.Attribute.MARGIN_TOP] = 0;
  lineStyle[DocumentApp.Attribute.FOREGROUND_COLOR] = SPIDA_RED;
  lineStyle[DocumentApp.Attribute.ITALIC] = false;

  body.appendParagraph(dataArray[0].replace('Event: ','')).setAttributes(titleStyle);
  body.appendParagraph('_____________________________________________________________________________________\n')
  .setAttributes(lineStyle);
};


//**********************************************putEventInfo()********************************************
function putEventInfo(dataArray, body){

  var type = body.appendParagraph('Type: ').setAttributes(PLAIN_TEXT).setBold(true);
  type.appendText( dataArray[1].replace('Type: ', '') ).setBold(false);


  var eventLevel = body.appendParagraph('Level: ').setAttributes(PLAIN_TEXT).setBold(true);
  eventLevel.appendText(dataArray[2].replace('Level: ', '')).setBold(false);


  var condition = dataArray[3].replace('Condition: ', '');

  if (condition !== 'Non-Conditional Event'){
    var eventCondition = body.appendParagraph('Condition: ').setAttributes(PLAIN_TEXT).setBold(true);
    eventCondition.appendText(dataArray[3].replace('Condition: ', '')).setBold(false);
  };

  var pEvents = new Array();
  pEvents = dataArray[4].replace('Next Possible Events: ', '').split('~');

  if (pEvents[0] !== 'None'){
    var posEvents = body.appendParagraph('Next possible events: ').setAttributes(PLAIN_TEXT).setBold(true);
    for (var i = 0; i < pEvents.length; i++){
      body.appendListItem(pEvents[i]).setAttributes(LIST_STYLE);
    };
  };

  body.appendHorizontalRule();
};



//**********************************************putActionInfo()***************************************
function putActionInfo(dataArray, body){

  var sActions = new Array(), pActions = new Array(), fActions = new Array();
  sActions = (dataArray[5].replace('On start: ', '')).split('~');
  pActions = (dataArray[6].replace('Planned: ', '')).split('~');
  fActions = (dataArray[7].replace('On finish: ', '')).split('~');

  var sNone = sActions[0] === 'None for this event';
  var pNone = pActions[0] === 'None for this event';
  var fNone = fActions[0] === 'None for this event';

  if ( !(sNone && pNone && fNone) ){


    var actionHeader = body.appendParagraph("Actions").setAttributes(HEADER_STYLE);

    if (!sNone){
      body.appendParagraph('On Start:').setAttributes(PLAIN_TEXT).setBold(true);
      for (var i = 0; i < sActions.length; i++){
        body.appendListItem(sActions[i]).setAttributes(LIST_STYLE)
      }
    };
    if (!pNone){
      body.appendParagraph('Planned: ').setAttributes(PLAIN_TEXT).setBold(true);
      for (var j = 0; j < pActions.length; j++){
        body.appendListItem(pActions[j]).setAttributes(LIST_STYLE)
      };
    };
    if (!fNone){
      body.appendParagraph('On finish: ').setAttributes(PLAIN_TEXT).setBold(true);
      for (var k = 0; k < fActions.length; k++){
        body.appendListItem(fActions[k]).setAttributes(LIST_STYLE)
      };
    };
    body.appendHorizontalRule();
  };
};

//**********************************************putFormInfo()***************************************
function putFormInfo(dataArray, body){
  var forms = new Array();
  forms = dataArray[8].split('~');

  var noForms = (forms[0] === 'No forms for this event');

  if (!noForms){

    var formHeader = body.appendParagraph("Forms").setAttributes(HEADER_STYLE);
    for (var i = 0; i < forms.length; i++){
      body.appendListItem(forms[i]).setAttributes(LIST_STYLE)
    };
    body.appendHorizontalRule();
  };
};

function putEventDescription(body){

  var description = body.appendParagraph("Description").setAttributes(HEADER_STYLE);
  body.appendParagraph('\n\tTODO').setAttributes(PLAIN_TEXT);

};


//**********************************************getEventOrder()***************************************
function getEventOrder(eventHtmls){

  var order, i=0;
  var orderText = eventHtmls.getFilesByName('order.txt');
  var eventOrder = new Array();

  while (orderText.hasNext() && i<1){
    i++;
    order = orderText.next();
    var orderString = order.getBlob().getDataAsString();
    Logger.log(orderString.toString());
    eventOrder = orderString.split('~');
  };

  return eventOrder;

};
