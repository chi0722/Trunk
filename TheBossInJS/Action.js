var ACTION_TYPE = require("./action_type");
var CITY_ID = require("./city_id");

function Action(type, cid, initFn, execFn) {

    this.type = type;
    this.cid = cid;
    this.execFn = execFn;

    initFn.call(this);
}

Action.prototype.filterOccasionals = function(gangsters) {
}

Action.prototype.toString = function() {

   return "[" + CITY_ID.toString(this.cid) + "] " +
          ACTION_TYPE.toString(this.type);
}

function ActionFactory() {};
ActionFactory.prototype.createAction = function(type, cid, reward) {

    var initFn;
    var execFn;
    switch (type) {
        case ACTION_TYPE.REWARD:
            initFn = function() {

                this.money = reward;
                this.addRewardListener = function(listener) {
                    this.listener = listener;
                }
                this.removeRewardListener = function() {
                    this.listener = null;
                }
                this.toString = function() {
                    return Action.prototype.toString.call(this) +
                           "(" + reward + ")";
                }
            }
            execFn = function(gangsterMap) { // boss to gangsters[]
                console.log("Rewarding..." + gangsterMap);
/*
                if (this.listener)
                    this.listener.reward(this.money);
                
                if (Object.getOwnPropertyNames(gangsterMap).length == 0)
                    return;

                var max = 0;
                var bossWhoWins = null;
                for (var boss in gangsterMap) {
                    var cur = gangstermap[boss].length;
                    if (cur > max) {
                        max = cur;
                        bossWhoWins = boss;
                     } else if (cur == max) {
                         bossWhoWins = null;
                     }
                }

                if (bossWhoWins)
                    bossWhoWins.reward(this.money);*/
            }
            break;            
        case ACTION_TYPE.IMPRISON:
            
            initFn = function() {
            }
            execFn = function(gangsterMap) { // boss to gangsters[]
                console.log("Imprisoning..." + gangsterMap);
            }
            break;
        case ACTION_TYPE.HOSPITALIZE:
            
            initFn = function() {
            }
            execFn = function(gangsterMap) { // boss to gangsters[]
                console.log("Hospitalizing..." + gangsterMap);
            }
            break;
        case ACTION_TYPE.SHOOT:
            
            initFn = function() {
            }
            execFn = function(gangsterMap) { // boss to gangsters[]
                console.log("Shooting..." + gangsterMap);
            }
            break;
        case ACTION_TYPE.BANISH:
            
            initFn = function() {
            }
            execFn = function(gangsterMap) { // boss to gangsters[]
                console.log("Banishing..." + gangsterMap);
            }
            break;
        default:
            throw "NOT a valid ACTION_TYPE: " +
                  ACTION_TYPE.toString(type);
    }
    return new Action(type, cid, initFn, execFn);
}

module.exports = new ActionFactory();
