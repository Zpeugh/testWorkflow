//globally defined username and password for logging in.
var USERNAME = 'admin@spidasoftware.com';
var PASSWORD = 'defaultADMIN321';
var WEBSITE = 'www.spidasoftware';
var START_ON_TAB = 0; //tab to begin looking for forms on.

//Array of form names to be screencaptured
var formNameArray = [];

var fs = require('fs');
var re = new RegExp('~', 'g');

//Set up the Casper object for the page
var casper = require('casper').create({
    stepTimeout: 8000,
    timeout : 10000,
    verbose: false,
    logLevel: 'error',
    pageSettings: {
      localToRemoteUrlAccessEnabled : true
    },
    onStepTimeout : function () {
        console.log("step timed out");
        this.navigationRequested = false;
        this.loadInProgress = false;
    },
    onTimeout : function () {
        console.log("script timed out");
        this.navigationRequested = false;
        this.loadInProgress = false;
    },
    onWaitTimeout : function () {
        console.log("wait timed out");
        this.navigationRequested = false;
        this.loadInProgress = false;
    }
});


casper.on('resource.received', function(resource) {

    if (resource.url.indexOf('Atmosphere-tracking-') > -1) {
        //console.log("Long-Polling cancellation");
        this.loadInProgress = false;
    }
});


//Take the arguments from the commandline and store them as the names of the forms
//in the formName array
var counter = 1;
var companyName = casper.cli.get(0).replace(re,' ');
console.log("Forms to capture: ");

while (casper.cli.has(counter)){

    formNameArray[counter - 1] = casper.cli.get(counter).replace(re,' ');
    console.log(counter++ + ': ' + formNameArray[counter - 2]);
};
if(formNameArray.length === 0){
    casper.exit();
};



/*************************************SCRIPT FUNCTIONS**********************************/

casper.logIntoSpida = function(){

    this.fillSelectors('form#fm1', {
            'input[name="username"]' : USERNAME,
            'input[name="password"]' : PASSWORD}, true);
};

/*
* Function nextTab(int tab)
* Take parameter tab, and go to that tab on the form list.  Iterate through each form on the page
* and then navigate to the form page to take a screenshot of any forms matching those
* in the formNameArray that are found on this tab
*/
casper.nextTab = function(tab) {
    casper.thenOpen('https://' + WEBSITE + '.com/projectmanager/formTemplate/list?offset=' + (tab * 10) + '&max=10&sort=name&order=asc', function(){

        this.waitForUrl('https://' + WEBSITE + '.com/projectmanager/formTemplate/list?offset=' + (tab * 10) + '&max=10&sort=name&order=asc', function(){
            //find number of forms on the tab
            var numberOfChildren = this.evaluate(function() {
                return document.querySelector('tbody').childNodes.length;
            });
            numberOfChildren = (numberOfChildren - 1) / 2;
            for (var j = 1; j <= numberOfChildren; j++){
                casper.matchFormNames(j);
            };
        }, function() {

            this.reload();
            this.wait(5000, function(){
                //find number of forms on the tab
                var numberOfChildren = this.evaluate(function() {
                    return document.querySelector('tbody').childNodes.length;
                });
                numberOfChildren = (numberOfChildren - 1) / 2;
                for (var j = 1; j <= numberOfChildren; j++){
                    casper.matchFormNames(j);
                };
            });

        }, 6000);
    });
};

/*
* Function matchFormNames(int childNumber)
* For every form name matching one in the formNameArray, navigate to the form page and take a sceenshot of it.
*/
casper.matchFormNames = function(childNumber){

    //console.log("outer: " + count);
    var totalForms = formNameArray.length;

    casper.each(formNameArray, function(casper, formName){

        //console.log('inner: ' + count);
        if(formName === this.fetchText('tr:nth-child(' + childNumber + ')>td>a')){

            //click on the form
            casper.then( function clickForm(){
                this.clickLabel(formName);
            });
            casper.waitForSelector('#wrap>div.wide-body', function() {
                this.evaluate(function(formName){
                    var elem = document.querySelector('#tabs>ul');
                    elem.style.maxHeight = "0px";
                    elem.innerHTML = "";
                    elem = document.getElementById('edittab');
                    elem.innerHTML = "";
                    elem = document.getElementsByClassName('tblheadcolor');
                    var htmlText, replacedHtml;
                    for (var i = 0; i < elem.length; i++){
                        htmlText = elem[i].innerHTML;
                        replacedHtml = htmlText.replace(new RegExp('<th>','g'),'<th class=\"whitefont\" >');
                        elem[i].innerHTML = replacedHtml;
                    };
                }, formName);
            }, function() {
                console.log("Missing Form.");
            }, 6000);

            //store form screenshot as 'formName.png'
            casper.then(function() {
                var html = this.getHTML('#wrap>div.wide-body');
                //console.log(formName + ':\n\n' + html);
                var selectRE = new RegExp('<select name=','g');
                html = html.replace(selectRE,'<select class=\"selectworkaround\" name=');
                html = html.replace('<h1>Form Template Editor : ' + formName + '</h1>', "<link href=\"flow.css\" rel=\"stylesheet\" type=\"text/css\" />" );
                html = html.replace("style=\"min-width:1100px\"", "style=\"min-width:800px\"");
                urlRE = new RegExp('/projectmanager/plugins/layout-3.7/images/16', 'g');
                html = html.replace(urlRE, '../images');

                fs.write('Resources/formHtmls/' + formName + '.html', html, 'w');
            });
            //navigate back to the form list page
            casper.then( function() {
                this.back();
            });

            casper.waitForSelector('#wrap>div.body>div:nth-child(2)>span>a', function() {
            }, function() {}, 6000);
            console.log("Stored screenshot of form: " + formName);
        };
    });
};


//*****************************BEGINNING OF SCRIPT*******************************//
/*
* Login to SpidaMin
*/
casper.start('https://' + WEBSITE + '.com/projectmanager/');


/*
* Login to SpidaMin
*/
casper.then(function login(){
    this.logIntoSpida();
});


casper.waitForUrl('https://' + WEBSITE + '.com/projectmanager/', function afterLogin(){
}, function onUrlTimeout(){
    casper.logIntoSpida();
}, 6000);


casper.waitForSelector('#mainnav>ul>li>div>div', function(){
    if (companyName !== 'SPIDA'){
        this.clickLabel(companyName);
    }
}, function(){
    this.reload();
    this.wait(4000, function(){
        if (companyName !== 'SPIDA'){
            this.clickLabel(companyName);
        }
    });
}, 6000);


casper.waitForUrl('https://' + WEBSITE + '.com/projectmanager/dashboard/index', function(){
    console.log("Logged in as: " + companyName);
}, function() {
    console.log('Should only see this message if company is SPIDA.')
}, 6000);


/*
* Navigate to the flow list
*/
casper.thenOpen('https://' + WEBSITE + '.com/projectmanager/formTemplate/list', function openFormList(){
    console.log("Navigating to Flow Form List");
});


/*
* Wait 7 seconds or until the url has changed
*/
casper.waitForUrl('https://' + WEBSITE + '.com/projectmanager/formTemplate/list',function waitForFormList(){
    console.log("Currently at: " + this.getCurrentUrl() );
}, function() {
    console.log("Currently at: " + this.getCurrentUrl() + " (timed out)" );
}, 6000);


/*
*Find the number of tabs in the form list, then iterate through them, capturing each form
*from the given array (formNameArray) on each page.
*/
casper.waitForSelector('#wrap>div.body>div.paginate_buttons', function loopThrough() {
    var numberOfTabs = this.evaluate(function() {
      return document.querySelector('#wrap>div.body>div.paginate_buttons').childNodes.length;
    },6000);

    //set tabs to number of children minus the current, previous, and next children
    numberOfTabs = numberOfTabs - 3;

    if (numberOfTabs < 1){
        this.nextTab(0);
    } else {
        for (var tab = 0; tab < numberOfTabs; tab++ ){
            this.nextTab(tab);
        };
    };
}, function(){

    this.reload();
    this.wait(5000, function(){
        var numberOfTabs = this.evaluate(function() {
          return document.querySelector('#wrap>div.body>div.paginate_buttons').childNodes.length;
        },6000);

        //set tabs to number of children minus the current, previous, and next children
        numberOfTabs = numberOfTabs - 3;

        if (numberOfTabs < 1){
            this.nextTab(0);
        } else {
            for (var tab = 0; tab < numberOfTabs; tab++ ){
                this.nextTab(tab);
            };
        };
    });
}, 6000);

/*
* Execute the script
*/
casper.run(function() {
    casper.exit();
});
