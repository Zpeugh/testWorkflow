var casper = require('casper').create({
    waitTimeout: 8000,
    stepTimeout: 8000,
    timeout: 12000,
    verbose: false,
    logLevel: 'error',
    pageSettings: {
    localToRemoteUrlAccessEnabled: true
    },
    onStepTimeout: function() {
    console.log("step timed out");
    this.navigationRequested = false;
    this.loadInProgress = false;
    },
    onTimeout: function() {
    console.log("script timed out");
    this.navigationRequested = false;
    this.loadInProgress = false;
    },
    onWaitTimeout: function() {
    console.log("wait timed out");
    this.navigationRequested = false;
    this.loadInProgress = false;
    }
});


casper.on('remote.message', function(msg) {
    this.echo('remote message caught: ' + msg);
});

//Take the arguments from the commandline and store them as the names of the actions
//in the formName array
var counter = 0;
var formNameArray = [];
var fs = require('fs');
var re = /\+/g;
var temp;

console.log('Forms to generate: ');

while (casper.cli.has(counter)) {
    temp = decodeURIComponent( casper.cli.get(counter) );
    formNameArray[counter] = temp.replace(re, ' ');
    console.log(counter + ': ' + formNameArray[counter] );
    counter++;
};

if (formNameArray.length === 0) {
    casper.exit();
};


//*****************************SCRIPT FUNCTIONS*******************************//

casper.changeSyntax = function(formName){
    var fName = formName.toString().replace(/\'/g, '[squote]');
    fName = fName.replace(/\\\\/g, '[bslash]');
    fName = fName.replace(/\"/g, '[dquote]');
    fName = fName.replace(/\//g, '[fslash]');
    fName = fName.replace(new RegExp("\\?", "g"), '[question]');
    fName = fName.replace(/:/g, '[colon]');
    return fName;
};


casper.capturePNG = function(formName){
    var fName = this.changeSyntax(formName);
    this.waitForSelector('#tabs', function(){
        var formSnip = this.getElementBounds('#tabs');
        this.capture('build/Resources/formHtmls/formPNGs/' + fName + '.png', {
            top : formSnip.top,
            height : formSnip.height,
            left : formSnip.left,
            width : formSnip.width
        });
        console.log('Created PNG of: '+ formName);
    }, function(){
        console.log('Could not find form: ' + formName);
    }, 3000);
};

casper.createFormHTML = function(formName, formValues) {

  this.then(function() {
    var fName = this.changeSyntax(formName);

    var fileName = fs.workingDirectory + '/build/Resources/formHtmls/' + fName + '.html';

    var headerString = '<html>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../src/Resources/form.css\"/>' +
      '\n\t<div class=\"header\">\n\t\t<hr>\n\t\t<h2>FORM: ' + formName.toUpperCase() +
      '</h2>\n\t\t<hr>\n\t</div>\n\t<div class=\"form\"><p>Description: </p>\n\t<div>' +
      '\n\t<center>\n\t\t<p class=\"underline\">Form Screenshot</p>\n\t\t<img src=\"file://' +
      fs.workingDirectory + '/build/Resources/formHtmls/formPNGs/' +
      fName + '.png\" >\n\t</div>\n\t<ul>Form Field Names and Descriptions:';

    fs.write(fileName, headerString, 'w');

    for (var i = 0; i < formValues.length; i++) {
        fs.write(fileName, '\n\t\t<li>' + formValues[i] + ':</li>', 'a');
    };
    fs.write(fileName, '\n\t</ul>\n</div>\n</html>', 'a');
  });
};

casper.getFormLabels = function(formName) {

    var formValues = ["temp"];
    var finalFormValues = [];
    var count = 0;
    do  {
        formValues[count] = this.evaluate(function(count) {
            var strongValues = document.getElementsByTagName('strong');
            return strongValues[count].valueOf().innerHTML;
        }, count);
        count++;
    }while (formValues[count-1]);

    for (var i = 0; i < formValues.length; i++){
        if (isNaN(formValues[i])){
            finalFormValues.push(formValues[i]);
        };
    };

    this.createFormHTML(formName, finalFormValues);
};

//*****************************BEGIN THE SCRIPT*******************************//
casper.start();

//Gets all of the fields in the action form and stores them in an array
casper.each(formNameArray, function(casper, formName) {
    var fName = this.changeSyntax(formName);
    var fileString =  'build/Resources/formHtmls/' + fName + '.html';

    casper.thenOpen(fileString, function(){
        this.capturePNG(formName);
        //console.log('file://' + fs.workingDirectory + '/build/Resources/formHtmls/' + formName + '.html')
        this.waitForSelector('#tabs' , this.getFormLabels(formName) );
    });
});

casper.run(function() {
  casper.exit();
});
