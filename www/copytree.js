var copytreeName = 'CopyTreePlugin';
var exec = require('cordova/exec');

module.exports = {
    getFilesFromFileChooser: function(success, fail) {
        exec(success, fail, "CopyTreePlugin", "getFilesFromFileChooser", []);
    },

    copyToInternal: function(externalPath, success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToInternal", [externalPath]);
    },

    copyToExternal: function(externalPath, includeDirs, success, fail) {
        exec(success, fail, "CopyTreePlugin", "copyToExternal", [externalPath, includeDirs]);
    }
}