var BossFactory = require('./boss');
var CITIES = require('./city').CITIES;
var PROTECTOR = require('./city').PROTECTOR;

/* game logic */

Array.prototype.shuffle = function() {
    var j, x, i = this.length - 1;
    while (i) {
        j = Math.floor(Math.random() * i);
        x = this[i];
        this[i] = this[j];
        this[j] = x;
        i--;
    }
}

function nextHand(bosses) {

    var allActions = [];
    for (var i = 0; i < numOfCities; i++) {
        var cards = CITIES[i].actions;
        var len = cards.length;
        var keepIndex = Math.floor(Math.random() * len);

        CITIES[i].nextHand();

        for (var j = 0; j < len; j++) {
            if (j === keepIndex)
                continue;
            allActions.push(cards[j]);
        }
    }

    PROTECTOR.nextHand();

    if (protectIndex != -1)
        CITIES[protectIndex].removeProtector(PROTECTOR);

    if (protectIndex < numOfCities - 1)
        protectIndex++;

    CITIES[protectIndex].addProtector(PROTECTOR);

    allActions.shuffle();

    var numOfCardsEach = allActions.length / bosses.length;
    for (i = 0; i < bosses.length; i++) {
        bosses[i].nextHand(
                allActions.splice(0, numOfCardsEach));
        console.log(bosses[i].toString());
    }
}

/* Globe variable definition */
var bosses = [];
var bossesInTurns = [];
var sockets = [];

var numOfBosses;
var numOfCities;
var protectIndex;
var currentTurnIndex;
var turnOffset;
var numOfRoundsInAHand;

/* FSM */
function State(name, handleFn, createActFn) {

    this.name = name;
    this.handleFn = handleFn;
    createActFn.call(this);
}

State.prototype.toString = function() { return this.name; };
State.prototype.isSupport = function(action) {
    
    return (typeof this.supportActions[action] != 'undefined');
}

var initState = new State(
        'Initial State',
        function handle() {
            numOfBosses = 3;
            numOfCities = numOfBosses * 2 - 1;

            protectIndex = -1;
            currentTurnIndex = 0;
            turnOffset = numOfBosses - 1;

            BossFactory.reset();
        },
        function actions() {
            this.supportActions = {};
            this.supportActions['register'] = function(name) {
            
                checkState('register');

                sockets.push(this);            
                console.log('Someone registers: ' + name);

                var b = BossFactory.createBoss(name);
                bosses.push(b);
                sockets[b.id].emit('init', b.id, numOfBosses);
                if (BossFactory.getTotalId() === numOfBosses)
                    stateChange(dispatchState);
            }
        });

var dispatchState = new State(
        'Dispatch State',
        function handle() {

            // first one change to the next            
            turnOffset = (turnOffset + 1) % numOfBosses;
            numOfRoundsInAHand = 0;

            // dispatch cards
            nextHand(bosses);

            // only show city of cards to other users
            for (var id = 0; id < sockets.length; id++) {
                var s = sockets[id];
                var b = bosses[id];
                s.broadcast.emit(
                    'list',
                    { id: id,
                      name: b.name,
                      actionCities: b.getActionCities(),
                      leftExperts: b.expertsInHand.length,
                      expertsInPrison: b.expertsInPrison.length,
                      expertsInHospital: b.expertsInHospital.length
                    });
            }

             // ensure that DISPATCH is received after LIST
            for (id = 0; id < sockets.length; id++) {
                var s = sockets[id];
                var b = bosses[id];
                s.emit(
                    'dispatch',
                    { actions: b.actionsInHand,
                      leftExperts: b.expertsInHand.length,
                      expertsInPrison: b.expertsInPrison.length,
                      expertsInHospital: b.expertsInHospital.length
                    });
            }

            bossesInTurns = bosses.slice(); // array copy
            stateChange(turnState);         
        },
        function actions() {});

var turnState = new State(
        'Turn State',
        function handle() {},
        function actions() {
            this.supportActions = {};
            this.supportActions['unveil'] = function() {
                checkState('unveil');
            }
        });

var occupyState = new State(
        'Occupy State',
        function handle() {},
        function actions() {
            this.supportActions = {};
            this.supportActions['occupy'] = function() {
                checkState('occupy');
            }
            this.supportActions['protect'] = function() {
                checkState('protect');
            }
            this.supportActions['pass'] = function() {
                checkState('pass');
            }
        });

var scoreState = new State(
        'Score State',
        function handle() {},
        function actions() {});

var endState =  new State(
        'End State',
        function handle() {},
        function actions() {});

var states = [ initState,
               dispatchState,
               turnState,
               occupyState,
               scoreState,
               endState ]; 
var currentState;

function checkState(action) {
    
    if (!currentState.isSupport(action))
        throw currentState + " DON'T support " + action;
    return true;
}

function stateChange(state) {

    console.log('State change to: ' + state.toString());
    currentState = state;
    state.handleFn();
}

/* web server configuration */
var express = require('express');
var app = express();
var sio = require('socket.io')(app.listen(3000, function() {
    console.log('Listen on 3000');
}));

app.use(express.static(__dirname + '/public'));
app.use('/module', express.static(__dirname));
/*
app.get('/', function(req, rsp) {
    rsp.send('test');
});
*/

sio.use(function(socket, next) {
    var debug = require('debug')('socket.io:user');
    debug('user use function');
    if (sockets.length == numOfBosses)
        return next(new Error('Exceed user limit'));
    next();
});


sio.on('error', function(error) {
    console.log('Error - ' + error);
});

sio.on('connect', function(socket) {

    socket.on('disconnect', function() {
    });

    states.forEach(function(state) {
    //for (var i = 0; i < states.length; i++) {
        var actions = state.supportActions;
        for (var act in actions) {
            (function(handler) {
                socket.on(act, function() {
                    handler.apply(socket, arguments);
                });
            })(actions[act]);
        }
    });
});


// game start!
stateChange(initState);

