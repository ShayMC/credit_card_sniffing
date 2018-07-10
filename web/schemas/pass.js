var mongoose = require('mongoose');
var Pass = new mongoose.Schema({
    password: String,
});
module.exports = mongoose.model('Pass', Pass);