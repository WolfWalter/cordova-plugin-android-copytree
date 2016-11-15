var copytreeName = 'CopyTreePlugin';
var exec = require('cordova/exec');

module.exports = {
    copyToInternal: function(showFileChooser, success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToInternal", [showFileChooser]);
    },

    copyToExternal: function(success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToExternal", []);
    }
}