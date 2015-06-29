//globally defined username and password for logging in.
var USERNAME = 'admin@spidasoftware.com';
var PASSWORD = 'defaultADMIN321';
var WEBSITE = 'www.spidasoftware'; //or demo.spidastudio
var actionNameArray = [];
var re = new RegExp('~', 'g');
var START_ON_TAB = 0; //tab to begin looking for forms on.

//create the file writing variable fs
var fs = require('fs');

//Set up the Casper object for the page
var casper = require('casper').create({

    stepTimeout: 8000,
    timeout : 10000,
    verbose: true,
    logLevel: 'debug',
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
var companyName = casper.cli.get(0).replace(re,' ');
var FLOW_NAME = casper.cli.get(1).replace(re,' ');
console.log("company name: " + companyName);
console.log("flow Name: " + FLOW_NAME);

while (casper.cli.has(counter)){
    actionNameArray[counter - 2] = casper.cli.get(counter).replace(re,' ');
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
    } else {
        // console.log("Requested Url: " + resource.url);

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
            this.capture('pngs/'+actionName+'.png');
            var html = this.getHTML('#action');
            html = html.replace("<form", "<html><link href=\"../flow.css\" rel=\"stylesheet\" " +
            "type=\"text/css\" /><div class=\"row\"><div id=\"action\" class=\"small-12 columns\"><form" );
            html = html.replace("</form>", "</div></div></form></html>");
            var imageRE = new RegExp('images', 'g');
            html = html.replace(imageRE,'../images');
            fs.write('Resources/actionHtmls/' + actionName + '.html', html, 'w');
            console.log("Captured action: " + actionName + "value: " + actionValue);

        }, function(){
            console.log('Missed action: ' + actionName);
        }, 6000);
    });
};


//*****************************BEGINNING OF SCRIPT*******************************//
casper.start('https://' + WEBSITE + '.com/projectmanager/');


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
    //this.capture('loggedIn2.png');
}, function() {
    console.log("Should only see this message if the company is SPIDA.");
}, 7000);


/*
* Navigate to the flow list
*/
casper.thenOpen('https://' + WEBSITE + '.com/projectmanager/flow/list', function openFlowList(){
    console.log("Navigating to Flow List");
    //this.capture('whatever.png');
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
    fs.write('Resources/Events/order.txt', 'Start', 'w');
    while (this.exists('#events_table>table>tbody>tr:nth-child(' + i + ')>td:nth-child(7)>div>a')){
        text = this.fetchText('#events_table>table>tbody>tr:nth-child(' + i + ')>td:nth-child(7)>div>a');
        fs.write('Resources/Events/order.txt', '~' + text, 'a');
        i++;
    };
}, function reloadPage(){
    console.log('Could not get event order');
}, 6000);


casper.wait(2000, function clickEdit(){
    console.log("Currently at: " + this.getCurrentUrl() );
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
				console.log('Capturing action: ' + currentAction + '...');
                actionValue = this.getElementAttribute('#possibleActionsSelect>option:nth-child(0)', 'value');
				console.log(actionValue);
				casper.getAction(1, actionName, actionValue);
            };
    	});
    } else {
        for (var j = 0; j <= numChildren; j++){
            casper.each(actionNameArray, function sendActionToGetCaptured(casper, actionName){
    			currentAction = this.fetchText('#possibleActionsSelect>option:nth-child(' + j + ')');
    			if(actionName === currentAction){
    				console.log('Capturing action: ' + currentAction + '...');
                    actionValue = this.getElementAttribute('#possibleActionsSelect>option:nth-child(' + j + ')', 'value');
    				console.log(actionValue);
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
