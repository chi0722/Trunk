package util;


public enum MessageType {

    /* Boss to Board*/
    REGISTER,	// register boss's info
    UNVEIL,
    OCCUPY,
    PROTECT,
    PASS,

    /* Board to Boss */
    EXCEPTION,      // io exception occurs
    INIT,			// notify an unique ID, number of users
    LIST,			// notify all bosses' info
    DISPATCH,		// dispatch your actions
    TURN,			// your turn
    UPDATE_UNVEIL,	// update action to unveil from others
    UPDATE_OCCUPY,	// update occupancy status from others
    UPDATE_PROTECT,	// update protector status from others
    UPDATE_PASS,
    FINISH_HAND,	// notify boss to wait for score
    LAST_UNVEIL,	// show the last unveiled action
    SCORE,			// notify rewards and sanctions
    END,			// game over...
    _TOTAL
}
