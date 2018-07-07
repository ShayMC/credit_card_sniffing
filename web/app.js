var express = require('express');
var path = require('path');
var logger = require('morgan');
var mongoose = require('mongoose');
mongoose.Promise = require("bluebird");

var cards = require('./routes/cards');


var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/cards', cards);



var myLessCompiler = require("./tools/less_compiler");
myLessCompiler();


var mongoDB = 'mongodb+srv://damir:damiri@cluster0-5kimc.mongodb.net/test?retryWrites=true';
mongoose.connect(mongoDB, {
        // useMongoClient: true
    }
);

var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error!\n'));

app.get('/', function (req, res) {
    res.sendFile(path.join(__dirname + "/index.html"));
});


app.listen(3000, function () {
    console.log("listening on 3000");
});

module.exports = app;
