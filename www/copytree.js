var copytreeName = 'CopyTreePlugin';
var exec = require('cordova/exec');

module.exports = {
    copyToInternal: function(success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToInternal", []);
    },

    copyToExternal: function(success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToExternal", []);
    }
}