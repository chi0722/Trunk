/* Boss to Board*/
exports.REGISTER = 'register';// register boss's info
exports.UNVEIL = 'unveil';
exports.OCCUPY = 'occupy';
exports.PROTECT = 'protect';
exports.PASS = 'pass';

    /* Board to Boss */
exports.EXCEPTION,      // io exception occurs
exports.INIT,			// notify an unique ID, number of users
exports.LIST,			// notify all bosses' info
exports.DISPATCH,		// dispatch your actions
exports.TURN,			// your turn
exports.UPDATE_UNVEIL,	// update action to unveil from others
exports.UPDATE_OCCUPY,	// update occupancy status from others
exports.UPDATE_PROTECT,	// update protector status from others
exports.UPDATE_PASS,
exports.FINISH_HAND,	// notify boss to wait for score
exports.LAST_UNVEIL,	// show the last unveiled action
exports.SCORE,			// notify rewards and sanctions
exports.END,			// game over...
