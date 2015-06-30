var casper = require('casper').create({
    waitTimeout: 8000,
    stepTimeout: 8000,
    timeout: 12000,
    verbose: true,
    logLevel: 'debug',
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
var re = new RegExp('~', 'g');

console.log('Forms to generate: ');

while (casper.cli.has(counter)) {

    formNameArray[counter] = casper.cli.get(counter).replace(re, ' ');
    console.log(counter + ': ' + formNameArray[counter] );
    counter++;
};

if (formNameArray.length === 0) {
    casper.exit();
};


//*****************************SCRIPT FUNCTIONS*******************************//

casper.capturePNG = function(formName){

    this.waitForSelector('#tabs', function(){
        var formSnip = this.getElementBounds('#tabs');
        this.capture('Resources/formHtmls/formPNGs/' + formName + '.png', {
            top : formSnip.top + 5,
            height : formSnip.height - 4,
            left : formSnip.left,
            width : formSnip.width
        });
    }, function(){
        console.log('Likely a blank form html page.')
    }, 3000);
};

casper.createFormHTML = function(formName, formValues) {

  this.then(function() {

    var fileName = fs.workingDirectory + '/Resources/formHtmls/' + formName + '.html';

    var headerString = '<html>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"form.css\"/>' +
      '\n\t<div class=\"header\">\n\t\t<hr>\n\t\t<h2>FORM: ' + formName.toUpperCase() +
      '</h2>\n\t\t<hr>\n\t</div>\n\t<div class=\"form\"><p>Description: </p>\n\t<div>' +
      '\n\t<center>\n\t\t<p class=\"underline\">Form Screenshot</p>\n\t\t<img src=\"file://' +
      fs.workingDirectory + '/Resources/formHtmls/formPNGs/' +
      formName + '.png\" >\n\t</div>\n\t<ul>Form Field Names and Descriptions:';

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
    this.thenOpen('file://' + fs.workingDirectory + '/Resources/formHtmls/' + formName + '.html', function(){
        this.capturePNG(formName);
        //this.capture('Resources/formHtmls/formPNGs/' + formName + '.png');
        this.waitForUrl('file://' + fs.workingDirectory + '/Resources/formHtmls/' + formName + '.html', this.getFormLabels(formName));
    });
});

casper.run(function() {
  casper.exit();
});
