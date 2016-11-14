var copytreeName = 'CopyTree';

function CopyTree() {
}

CopyTree.prototype = {
    copyToInternal: function() {
        cordova.exec(success, fail, copytreeName, "copyToInternal");
    },

    copyToExternal: function() {
        cordova.exec(success, fail, copytreeName, "copyToExternal");
    }
};

module.exports = new CopyTree();
