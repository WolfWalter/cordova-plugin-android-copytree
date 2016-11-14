var copytreeName = 'CopyTreePlugin';

function CopyTree() {
}

CopyTree.prototype = {
    copyToInternal: function(success, fail) {
        cordova.exec(success, fail, copytreeName, "copyToInternal");
    },

    copyToExternal: function(success, fail) {
        cordova.exec(success, fail, copytreeName, "copyToExternal");
    }
};

module.exports = new CopyTree();
