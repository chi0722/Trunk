var ACTION_TYPE = require("./action_type");
var CITY_ID = require("./city_id");
var ActionFactory = require("./action");

function City(id, createActions) {

    this.id = id;
    this.gangsterMap = {}; // map from Boss to gangster list[]
    this.unShowenActions = [];

    // init
    createActions.call(this);
    this.nextHand();
}

City.prototype.toString = function() {
    var str = "/" + CITY_ID.toString(this.id) + "/\n";
    for (var i in this.actions) {
        str += ("\t" + this.actions[i] + "\n");
    }
    return str;
}

City.prototype.nextHand = function() {

    this.unShowenActions.length = 0; // empty array
    this.unShowenActions = this.actions.slice();
    //console.log("next hand:, unShowenActions = " + this.unShowenActions);

    // empty occupied gangsters;
    for (var i in this.gangsterMap)
        this.gangsterMap[i].length = 0;
}

City.prototype.removeProtector = function() {

    for (var i = 0; i< this.actions.length; i++) {
        var a = this.actions[i];
        if (a.type == ACTION_TYPE.REWARD)
            a.removeRewardListener();
    }
}

City.prototype.addProtector = function(protector) {

    for (var i = 0; i< this.actions.length; i++) {
        var a = this.actions[i];
        if (a.type == ACTION_TYPE.REWARD)
            a.addRewardListener(protector);
    }
}

City.prototype.occupy = function(boss, gangster) {

    if (!this.gangsterMap[boss])
        this.gangsterMap[boss] = [];
    this.gangsterMap[boss].push(gangster);
}

City.prototype.showAction = function(action) {

    console.log("Show Action: " + action.toString());
    this.unShowenActions.splice(this.unShowenActions.indexOf(action), 1);
}

module.exports.PROTECTOR = new City(
        CITY_ID.CHICAGO, 
        function() { this.actions = [];}
);

module.exports.PROTECTOR.reward = function(money) {

    var action = ActionFactory.createAction(
                    ACTION_TYPE.REWARD,
                    this.id,
                    Math.floor(money/2));
    action.execFn(this.gangsterMap);
}

module.exports.CITIES = [
    new City(CITY_ID.NEW_YORK, function() {
        this.actions = [];
        for (var i = 0; i <= 3; i++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, i));
        }
    }),

    new City(CITY_ID.BOSTON, function() {
        this.actions = [];
        for (var i = 0; i < 2; i ++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, 3));
        }
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.IMPRISON, this.id));
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.HOSPITALIZE, this.id));
    }),

    new City(CITY_ID.DETROIT, function() {
        this.actions = [];
        for (var i = 1; i <= 4; i++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, i));
        }
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.SHOOT, this.id));
    }),

    new City(CITY_ID.KANSAS_CITY, function() {
        this.actions = [];
        for (var i = 2; i <= 3; i++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, i));
        }
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.SHOOT, this.id));
    }),

    new City(CITY_ID.CINCINNATI, function() {
        this.actions = [];
        for (var i = 1; i <= 3; i ++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, i));
        }
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.BANISH, this.id));
    }),

    new City(CITY_ID.MEMPHIS, function() {
        this.actions = [];
        for (var i = 1; i <= 3; i ++) {
            this.actions.push(
                ActionFactory.createAction(
                    ACTION_TYPE.REWARD, this.id, i));
        }
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.HOSPITALIZE, this.id));
    }),

    new City(CITY_ID.PHILADELPHIA, function() {
        this.actions = [];
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.REWARD, this.id, 3));
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.SHOOT, this.id));
        this.actions.push(
            ActionFactory.createAction(
                ACTION_TYPE.IMPRISON, this.id));
    })
];

