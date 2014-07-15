$(function() {
    var FADE_TIME = 150; // ms
    var TYPING_TIMER_LENGTH = 400; // ms
    var COLORS = [
    '#e21400', '#91580f', '#f8a700', '#f78b00',
    '#58dc00', '#287b00', '#a8f07a', '#4ae8c4',
    '#3b88eb', '#3824aa', '#a700ff', '#d300e7'
    ];

    var $window = $(window);
    var $usernameInput = $('.usernameInput');
    var $messages = $('.messages'); // Messages area
    var $inputMessage = $('.inputMessage'); // Input message input box

    var $loginPage = $('.login.page'); // The login page
    var $chatPage = $('.chat.page'); // The chatroom page
    var $errorPage = $('.error.page');

    var username;
    var $currentInput = $usernameInput.focus();

    var socket = io('http://localhost:3000');

    $errorPage.hide();

    /*
    String.prototype.toWebString = function() {

        var arr = this.split('\n');
        var str = arr.join('<br/>');
        console.log(str);
        return str;
    }*/
    

    function setUsername () {
        username = cleanInput($usernameInput.val().trim());
        // If the username is valid
        if (username) {
            $loginPage.fadeOut();
            $chatPage.show();
            $loginPage.off('click');
            $currentInput = $inputMessage.focus();

            // Tell the server your username
            socket.emit('register', username);
        }
    }

    function cleanInput (input) {
        return $('<div/>').text(input).text();
    }

    // Adds the visual chat message to the message list
    function addChatMessage (data, options) {
        // Don't fade the message in if there is an 'X was typing'
        /*var $typingMessages = getTypingMessages(data);
        options = options || {};
        if ($typingMessages.length !== 0) {
            options.fade = false;
            $typingMessages.remove();
        }*/

        console.log("::" + data);
        var $usernameDiv = $('<div class="username"/>')
            .text(data.username)
            .css('color', getUsernameColor(data.username));
        var $messageBodyDiv = $('<div class="messageBody">')
            .text(data.message);
        $messageBodyDiv.html('<br/>' + 
                $messageBodyDiv.text().replace(/\n/g, '<br/>').replace(/\s/g, '&nbsp;'));
        var typingClass = data.typing ? 'typing' : '';
        var $messageDiv = $('<li class="message"/>')
            .data('username', data.username)
            .addClass(typingClass)
            .append($usernameDiv, $messageBodyDiv);
        addMessageElement($messageDiv, options);
    }

    // Adds a message element to the messages and scrolls to the bottom
    // el - The element to add as a message
    // options.fade - If the element should fade-in (default = true)
    // options.prepend - If the element should prepend
    //   all other messages (default = false)
    function addMessageElement (el, options) {
        var $el = $(el);

        // Setup default options
        if (!options) {
            options = {};
        }
        if (typeof options.fade === 'undefined') {
            options.fade = true;
        }
        if (typeof options.prepend === 'undefined') {
            options.prepend = false;
        }

        // Apply options
        if (options.fade) {
            $el.hide().fadeIn(FADE_TIME);
        }
        if (options.prepend) {
            $messages.prepend($el);
        } else {
            $messages.append($el);
        }
        $messages[0].scrollTop = $messages[0].scrollHeight;
    }

    function getUsernameColor (username) {
        // Compute hash code
        var hash = 7;
        for (var i = 0; i < username.length; i++) {
            hash = username.charCodeAt(i) + (hash << 5) - hash;
        }
        // Calculate color
        var index = Math.abs(hash % COLORS.length);
        return COLORS[index];
    }

    // Keyboard events

    $window.keydown(function (event) {
        // Auto-focus the current input when a key is typed
        if (!(event.ctrlKey || event.metaKey || event.altKey)) {
            $currentInput.focus();
        }
        // When the client hits ENTER on their keyboard
        if (event.which === 13) {
            console.log("key down " + username);
            if (username) {
                //sendMessage();
                //socket.emit('stop typing');
                //typing = false;
            } else {
                setUsername();
            }
        }
    });

    // Click events

    // Focus input when clicking anywhere on login page
    $loginPage.click(function () {
        $currentInput.focus();
    });


    // Global definition
    var numOfBosses;
    var numOfCities;
    var protectIndex;
    var myId;
    var snapshots = [];
    var myActions = [];

    // Snapshot
    function Snapshot(numOfBosses, id) {

        this.numOfCities = 2 * numOfBosses;
        this.name = "untitled";
        this.id = id;

        this.actionCids = [];
        this.expertsInCity = [];
        this.occasionalsInCity = [];
        this.expertsInHand = 0;
        this.expertsInHospital = 0;
        this.expertsInPrisonDay1 = 0;
        this.expertsInPrisonDay2 = 0;
        this.occasionalsInHand = 3;
        this.rewards = 0;

        this.nextHand(6, 0, 0);
    }

    Snapshot.prototype.toString = function() {

        var str = "";
        str += "Expert# = " + this.expertsInHand + "\n";
        str += "Occasional# = " + this.occasionalsInHand + "\n";
        str += "Hospital# = " + this.expertsInHospital + "\n";
        str += "Prison#(D1) = " + this.expertsInPrisonDay1 + "\n";
        str += "Prison#(D2) = " + this.expertsInPrisonDay2 + "\n";
        str += "$$ Reward $$ = " + this.rewards;
        return str;
    }

    Snapshot.prototype.occupyCity = function(cid, e, o) {

        this.expertsInCity[cid] += e;
        this.occasionalsInCity[cid] += o;
        this.expertsInHand -= e;
        this.occasionalsInHand -= o;
    }

    Snapshot.prototype.nextHand = function(hand, hospital, prison) {

        for (var i = 0; i < this.numOfCities + 1; i++) {
            this.expertsInCity[i] = 0;
            this.occasionalsInCity[i] = 0;
            // why need this in java version ??
            //this.actionCids.length = 0;
        }
        
        this.expertsInHand = hand;
        this.expertsInHospital = hospital;
        this.expertsInPrisonDay2 = this.expertsInPrisonDay1;
        this.expertsInPrisonDay1 = prison - this.expertsInPrisonDay2;
    }

    Snapshot.prototype.reward = function(money) {

        this.rewards += money;
    }

    // FSM
    function State(name, handleFn, createActFn) {

        this.name = name;
        this.handleFn = handleFn;
        createActFn.call(this);
    }

    State.prototype.toString = function() { return this.name; };
    State.prototype.isSupport = function(action) {

        return (this.supportActions[action] != -1);
    }

    var initState = new State(
            'Initial State',
            function handle() {

                protectIndex = -1;
            },
            function actions() {
                this.supportActions = {};
                this.supportActions['init'] = function(id, bossNum) {
       
                    checkState('init');

                    console.log("Myid = " + id + ", numOfBosses = " + bossNum);
                    myId = id;
                    numOfBosses = bossNum;
                    numOfCities = numOfBosses * 2 - 1;

                    snapshots[myId] = new Snapshot(numOfBosses, myId);
                    snapshots[myId].name = username;

                    stateChange(dispatchState);
                }
            });

    function nextHand() {

        myActions.length = 0;
    }

    function Action() {};
    Action.prototype.toString = function() {

        return "[" + CITY_ID.toString(this.cid) + "] " +
               ACTION_TYPE.toString(this.type);
    }
    function setActionProto(actions) {

        for (var i = 0; i < actions.length; i++) {
            actions[i].__protot__ = Action.prototype;
        }
    }

    function getSnapshotString() {

        var str = "";
        var tab = "    ";
        var id = myId;
        do {
            str += "= " + snapshots[id].name + " (id-" + id + ")=\n";
            if (id == myId) {
                str += (tab + ">>> me\n")
                for (var i = 0; i < myActions.length; i++) {
                    var act = myActions[i];
                    str += (tab + "[" + CITY_ID.toString(act.cid) + "] " +
                            ACTION_TYPE.toString(act.type) + "\n");
                }
            } else {
                for (i = 0; i < snapshots[id].actionCids.length; i++) {
                    var cid = snapshots[id].actionCids[i];
                    str += (tab + "[" + CITY_ID.toString(cid) + "]\n");
                }
            }
            id = (id + 1) % numOfBosses;
        } while (id != myId);

        str += "*** CITIES' STATUS ***\n";
        for (i = 0; i < numOfCities; i++) {
            str += (tab + CITY_ID.toString(i) + "\n");

            if (i >= protectIndex) {
                if (i == protectIndex)
                    str += (tab + "(Protector) " + 
                            CITY_ID.toString(CITY_ID.CHICAGO) + "\n");
                str += getOccupiedString(i + 1);
            } else
                str += getOccupiedString(i);
        }
        return str;
    }

    function getOccupiedString(cid) {

        var str = "";
        var tab = "    ";
        for (var id = 0; id < numOfBosses; id++) {
            var exp = snapshots[id].expertsInCity[cid];
            var occ = snapshots[id].occasionalsInCity[cid];
            if (exp != 0 || occ != 0) {
                str += (tab + snapshots[id].name + " occupies ");
                if (exp != 0 && occ !=0)
                    str += (exp + " Experts & " + occ + " Occasionals.");
                else if (exp != 0)
                    str += (exp + " Experts.");
                else
                    str += (occ + " Occasionals.");
            }
        }
        return str;
    }

    var dispatchState = new State(
            'Dispatch State',
            function handle() {

                if (protectIndex < numOfCities - 1)
                    protectIndex++;

                nextHand();
            },
            function actions() {
                this.supportActions = {};
                this.supportActions['list'] = function(data) {

                    checkState('list');

                    /* 
                    data = { id: id,
                      name: b.name,
                      actionCities: b.getActionCities(),
                      leftExperts: b.expertsInHand.length,
                      expertsInPrison: b.expertsInPrison.length,
                      expertsInHospital: b.expertsInHospital.length
                    } */
                    var s = snapshots[data.id] = new Snapshot(numOfBosses, data.id);
                    s.name = data.name;
                    s.actionCids = data.actionCities;
                    s.nextHand(data.leftExperts,
                               data.expertsInHospital,
                               data.expertsInPrison);
                }
                this.supportActions['dispatch'] = function(data) {

                    checkState('dispatch');

                    /* 
                    data = { actions: b.actionsInHand,
                      leftExperts: b.expertsInHand.length,
                      expertsInPrison: b.expertsInPrison.length,
                      expertsInHospital: b.expertsInHospital.length
                    } */
                    var s = snapshots[myId];
                    myActions = data.actions;
                    s.nextHand(data.leftExperts,
                               data.expertsInHospital,
                               data.expertsInPrison);
                    var d = { 
                        username:snapshots[myId].name,
                        message: getSnapshotString() };
                    addChatMessage(d);

                    stateChange(waitTurnState);
                }
                this.supportActions['end'] = function() {

                    checkState('end');

                    // Game over
                    stateChange(endState);
                }
            });

    var waitTurnState = new State(
            'WaitTurn State',
            function handle() {},
            function actions() {
                this.supportActions = {};
                this.supportActions['turn'] = function() {
                    checkState('turn');
                }
                this.supportActions['update_unveil'] = function() {
                    checkState('update_unveil');
                }
                this.supportActions['update_occupy'] = function() {
                    checkState('update_occupy');
                }
                this.supportActions['update_protect'] = function() {
                    checkState('update_protect');
                }
                this.supportActions['update_pass'] = function() {
                    checkState('update_pass');
                }
                this.supportActions['finish_hand'] = function() {
                    checkState('finish_hand');
                }
            });

    var scoreState = new State(
            'Score State',
            function handle() {},
            function actions() {
                this.supportActions = {};
                this.supportActions['score'] = function() {
                    checkState('score');
                }
                this.supportActions['last_unveil'] = function() {
                    checkState('last_unveil');
                }
            });

    var endState =  new State(
            'End State',
            function handle() {},
            function actions() {});

    var states = [ initState,
                   dispatchState,
                   waitTurnState,
                   scoreState,
                   endState ]; 
    var currentState;

    function checkState(action) {

        if (!currentState.isSupport(action))
            throw currentState + " DON'T support " + action;
    }

    function stateChange(state) {

        console.log('State change to: ' + state.toString());
        currentState = state;
        state.handleFn();
    }

    // client socket
    socket.on('connect', function(socket) {
        console.log('Connect!');
    });

    socket.on('error', function(reason) {
        console.log(reason);
        $loginPage.hide();
        $chatPage.hide();
        $errorPage.show();
        $loginPage.off('click');
    });

    for (var i = 0; i < states.length; i++) {
        var actions = states[i].supportActions;
        for (var act in actions) {
            (function(handler) {
                socket.on(act, function() {
                    handler.apply(socket, arguments);
                });
            })(actions[act]);
        }
    }

    stateChange(initState);
})
