var casper = require('casper').create({
    stepTimeout: 6000,
    timeout : 8000,
    verbose: false,
    logLevel: 'error',
    pageSettings: {
      localToRemoteUrlAccessEnabled : true
    },
    onStepTimeout : function () {
      this.capture('timeout.png');
      this.page.stop();
    },
    onTimeout : function () {
      console.log("script timed out");
      this.page.stop();
    },
    onWaitTimeout : function () {
      console.log("wait timed out");
      this.page.stop();
    }
});


//Take the arguments from the commandline and store them as the names of the actions
//in the actionName array
var counter = 0;
var actionNameArray = [];
var fs = require('fs');
var re = new RegExp('~', 'g');
var reUnder = new RegExp('_', 'g');
console.log("Actions to capture: ");

while ( casper.cli.has(counter) ){

    actionNameArray[counter] = casper.cli.get(counter).replace(re,' ');
    console.log(counter + ': ' + actionNameArray[counter]);
    counter++;
};

if(actionNameArray.length === 0){
    casper.exit();
};


//*****************************SCRIPT FUNCTIONS*******************************//


casper.getActionLabels = function(actionName){

    this.thenOpen('Resources/actionHtmls/' + actionName + '.html', function(){

          var actionFormValues = this.getElementsAttribute('div>label', 'for');
          for (var x = 0; x < actionFormValues.length ; x++){
              var label = actionFormValues[x];
              if (label.charAt(0) === label.charAt(0).toLowerCase()){
                actionFormValues[x] = label.charAt(0).toUpperCase() + label.substr(1,label.length) ;
              };

              actionFormValues[x] = actionFormValues[x].replace('Pm_', '');
              actionFormValues[x] = actionFormValues[x].replace(reUnder, ' ');
          };
          this.createActionHTML(actionName, actionFormValues);
    });
};


casper.createActionHTML = function(actionName, actionFormValues){
    this.thenOpen('Resources/actionHtmls/' + actionName + '.html', function(){

        var fileName = fs.workingDirectory + '/Resources/actionHtmls/' + actionName + '.html';
        this.createPageHeader(fileName, actionName, actionFormValues);

        for (var i = 0; i < actionFormValues.length; i++){
            fs.write(fileName, '\t\t<li>' + actionFormValues[i] + ':</li>\n', 'a');
        };
            fs.write(fileName, '\t</ul>\n</div>\n</html>', 'a');
    });
};


casper.createPageHeader = function(fileName, actionName, actionFormValues){

    var headerString = '<html>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"action.css\"/>' +
    '\n\t<div class=\"header\">\n\t\t<hr>\n\t\t<h2>ACTION: ' + actionName.toUpperCase() +
    '</h2>\n\t\t<hr>\n\t</div>\n\t<div class=\"action\"><p>Description: </p>\n\t<center>\n\t\t<img src=\"file://' +
    fs.workingDirectory + '/Resources/actionHtmls/actionPNGs/' +
    actionName + '.png\" >\n\t</center>\n\t<ul>';

    fs.write(fileName, headerString, 'w');
}


//*****************************BEGIN THE SCRIPT*******************************//


casper.makeActionPNG = function(actionName) {

    this.thenOpen('Resources/actionHtmls/' + actionName + '.html', function(){
        this.waitForSelector('#actionForm>fieldset', function(){

            var actionSnip = this.getElementBounds('#actionForm>fieldset');

            this.capture('Resources/actionHtmls/actionPNGs/' + actionName + '.png', {
                top : actionSnip.top - 3,
                height : actionSnip.height,
                left : (actionSnip.left + 47),
                width : (actionSnip.width - 47)
            });
        }, function missedFile() {
        console.log("Missing action file: " + actionName + ".html")
        }, 4000);
    });
};

casper.start();

//Gets all of the fields in the action form and stores them in an array
casper.each(actionNameArray, function(casper, actionName) {

   this.makeActionPNG(actionName);

});

casper.each(actionNameArray, function(casper, actionName) {

   this.getActionLabels(actionName);
});

casper.run( function(){
casper.exit();
});
