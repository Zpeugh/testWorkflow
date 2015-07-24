//globally defined username and password for logging in.
var USERNAME = 'admin@spidasoftware.com';
var PASSWORD = 'defaultADMIN321';
//var WEBSITE = 'www.spidasoftware';
var WEBSITE = 'demo.spidastudio';
var actionNameArray = [];
var re = /\+/g
var START_ON_TAB = 0; //tab to begin looking for forms on.

//create the file writing variable fs
var fs = require('fs');

//Set up the Casper object for the page
var casper = require('casper').create({

    stepTimeout: 8000,
    timeout : 10000,
    verbose: false,
    logLevel: 'error',
    pageSettings: {
      userAgent : "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36",
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


casper.on('page.error', function (msg, trace) {
    this.echo( 'Error: ' + msg, 'ERROR' );
});

//store the action names passed from the command line into actionNameArray
var counter = 2;
var companyName = decodeURIComponent(casper.cli.get(0));
companyName = companyName.replace(re, ' ');
var FLOW_NAME =  decodeURIComponent(casper.cli.get(1)).replace(re, ' ');
var temp;

console.log("company name: " + companyName);
console.log("flow Name: " + FLOW_NAME);

while (casper.cli.has(counter)){
    temp = decodeURIComponent(casper.cli.get(counter));
    actionNameArray[counter - 2] = temp.replace(re, ' ');
    console.log(counter + ': ' + actionNameArray[counter - 2]);
    counter++;
};
if(actionNameArray.length === 0){
    casper.exit();
};

casper.on('resource.received', function(resource) {

    if (resource.url.indexOf('Atmosphere-tracking-') > -1) {
        // console.log("ATMOSPHERE CANCELLED");
        this.loadInProgress = false;
    } else if (resource.url.indexOf('getSessionStatus') > -1){
        this.loadInProgress = false;
        console.log('Cancelled session update');

    };
});


//*******************************SCRIPT FUNCTIONS*******************************//

casper.logIntoSpida = function(){

    this.fillSelectors('form#fm1', {
            'input[name="username"]' : USERNAME,
            'input[name="password"]' : PASSWORD}, true);
};

casper.getAction = function(j, actionName, actionValue){

    casper.then(function changeOnClickValue(){

        casper.evaluate(function(actionValue){
            window.FlowPlumbing.editAction(actionValue);
        }, actionValue);
    });

    casper.wait(2000, function printPage(){
        this.waitForSelector('#actionForm>fieldset', function waitedForJavascript(){
            var aName = actionName.toString().replace(/\'/g, '[squote]');
            aName = aName.replace(/\\\\/g, '[bslash]');
            aName = aName.replace(/\"/g, '[dquote]');
            aName = aName.replace(/\//g, '[fslash]');
            aName = aName.replace(new RegExp("\\?", "g"), '[question]');
            aName = aName.replace(/:/g, '[colon]');
            var html = this.getHTML('#action');
            html = html.replace("<form", "<html><link href=\"../../../src/Resources/actionflow.css\" rel=\"stylesheet\" " +
            "type=\"text/css\" /><div class=\"row\"><div id=\"action\" class=\"small-12 columns\"><form" );
            html = html.replace("</form>", "</div></div></form></html>");
            var imageRE = new RegExp('images', 'g');
            html = html.replace(imageRE,'../../../src/Resources/images');
            fs.write('build/Resources/actionHtmls/' + aName + '.html', html, 'w');
            console.log("Stored screenshot of action: " + actionName );

        }, function(){
            console.log('Could not find action: ' + actionName);
        }, 6000);
    });
};


//*****************************BEGINNING OF SCRIPT*******************************//
casper.start('https://' + WEBSITE + '.com/projectmanager/', function(){
    console.log("Checkpoint reached");
});


/*
* Login to SpidaMin
*/
casper.then(function login(){
    this.logIntoSpida();
});


/*
* Wait 8 seconds for the form to be submitted and the page is done redirecting
*/
casper.waitForUrl('https://' + WEBSITE + '.com/projectmanager/', function afterLogin(){
    console.log('Logged in successfully');
    console.log("Checkpoint reached");
    //this.capture('loggedIn.png');
}, function onUrlTimeout(){
    //casper.capture('NoForm.png');
}, 6000);

casper.waitForSelector('#mainnav>ul>li>div>div', function(){
    if (companyName !== 'SPIDA'){
        this.clickLabel(companyName);
    };
}, function(){
    this.reload();
    this.wait(6000, function(){
        if (companyName !== 'SPIDA'){
            this.clickLabel(companyName);
        };
    });
}, 6000);

casper.waitForUrl('https://' + WEBSITE + '.com/projectmanager/dashboard/index', function(){
    console.log("Logged in as: " + companyName);
    console.log("Checkpoint reached");
}, function() {
    console.log("Should only see this message if the company is SPIDA.");
    console.log("Checkpoint reached");
}, 7000);


/*
* Navigate to the flow list
*/
casper.thenOpen('https://' + WEBSITE + '.com/projectmanager/flow/list', function openFlowList(){
    console.log("Navigating to Flow List");
});


/*
* Wait until the url has changed
*/
//NEED TO FIX BUG WHEN FLOW_NAME MATCHES ANOTHER LABEL ON THE PAGE
casper.waitForSelector('#wrap>div.body>div.list>table>tbody', function clickFlow(){
    console.log("Currently at: " + this.getCurrentUrl() );
    this.clickLabel(FLOW_NAME, 'a');
}, function FlowFailed(){
    //this.capture('failure.png');
    console.log("Resource failed to load, trying to reload page");
    this.thenOpen('https://' + WEBSITE + '.com/projectmanager/flow/list', function reloadFlowPage(){
        this.wait(7000, function(){
            this.clickLabel(FLOW_NAME, 'a');
        });
    });
}, 6000);

casper.waitForSelector('#events_table>table>tbody>tr:nth-child(1)>td:nth-child(5)>a', function getOrder(){
    var i = 2;
    var text;
    fs.write('build/Resources/Events/order.txt', 'Start', 'w');
    while (this.exists('#events_table>table>tbody>tr:nth-child(' + i + ')>td:nth-child(7)>div>a')){
        text = this.fetchText('#events_table>table>tbody>tr:nth-child(' + i + ')>td:nth-child(7)>div>a');
        text = text.replace(/\'/g, '[squote]');
        text = text.replace(/\\/g, '[bslash]');
        text = text.replace(/\"/g, '[dquote]');
        text = text.replace(/\//g, '[fslash]');
        text = text.replace(new RegExp("\\?", "g"), '[question]');
        text = text.replace(/:/g, '[colon]');
        fs.write('build/Resources/Events/order.txt', '~' + text, 'a');
        i++;
    };
}, function reloadPage(){
    console.log('Could not get event order');
}, 6000);


casper.wait(2000, function clickEdit(){
    console.log("Currently at: " + this.getCurrentUrl() );
    console.log("Checkpoint reached");
    this.click('#events_table>table>tbody>tr:nth-child(1)>td:nth-child(5)>a');
});

casper.waitForSelector('form>div>div>dl>dd:nth-child(2)>a', function clickActionTab() {
	//this.capture('clickedPencil.png');
    this.click('form>div>div>dl>dd:nth-child(2)>a');
}, function(){
    console.log('Could not find form, trying again.');
    casper.wait(2000, function clickEdit(){
        this.click('#events_table>table>tbody>tr:nth-child(1)>td:nth-child(5)>a');
    });
    casper.wait(5000, function clickEdit(){
        this.click('form>div>div>dl>dd:nth-child(2)>a');
    });

}, 10000);


casper.waitForSelector('#possibleActionsSelect', function chooseAnAction() {

    var numChildren = this.evaluate(function getNumberOfChildren() {
        return document.querySelector('#possibleActionsSelect').childNodes.length;
    });
	var currentAction;
	var actionValue;
    if (numChildren < 1){
        casper.each(actionNameArray, function sendActionToGetCaptured(casper, actionName){
			currentAction = this.fetchText('#possibleActionsSelect>option:nth-child(0)');
			if(actionName === currentAction){
                actionValue = this.getElementAttribute('#possibleActionsSelect>option:nth-child(0)', 'value');
				casper.getAction(1, actionName, actionValue);
            };
    	});
    } else {
        for (var j = 0; j <= numChildren; j++){
            casper.each(actionNameArray, function sendActionToGetCaptured(casper, actionName){
    			currentAction = this.fetchText('#possibleActionsSelect>option:nth-child(' + j + ')');
    			if(actionName === currentAction){
                    actionValue = this.getElementAttribute('#possibleActionsSelect>option:nth-child(' + j + ')', 'value');
    				casper.getAction(j, actionName, actionValue);
                };
        	});
    	};
    };
}, function(){

    console.log('Something went pretty wrong.')

}, 6000);



/*
* Execute the script
*/
casper.run(function() {
casper.exit();
});
