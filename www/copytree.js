var copytreeName = 'CopyTreePlugin';
var exec = require('cordova/exec');

module.exports = {
    getFilesFromFileChooser: function(success, fail) {
        exec(success, fail, "CopyTreePlugin", "getFilesFromFileChooser", []);
    },

    copyToInternal: function(showFileChooser, success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToInternal", []);
    },

    copyToExternal: function(includeDirs, success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToExternal", [includeDirs]);
    }
}