var express = require('express');
var path = require('path');
var logger = require('morgan');
var mongoose = require('mongoose');
mongoose.Promise = require("bluebird");

var cards = require('./routes/cards');


var app = express();

// // view engine setup
// app.set('views', path.join(__dirname, 'views'));
// app.set('view engine', 'ejs');

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

//
// let Pass = require('./schemas/pass');
// const bcrypt = require('bcrypt-nodejs');
// bcrypt.genSalt(10, function (err, salt) {
//     bcrypt.hash("erandamirshay", salt, null, function (err, hash) {
//        let psw = new Pass({
//            password : hash
//         });
//        psw.save();
//     })
// });






// var http = require('http');
// var qs = require('qs');
//
// var query = qs.stringify({
//     api_option: 'paste',
//     api_dev_key: '2d84d80877824d333fa9fac2cf786bc7',
//     api_paste_code: 'Awesome paste content',
//     api_paste_name: 'Awesome paste name',
//     api_paste_private: 1,
//     api_paste_expire_date: '1D'
// });
//
// var req = http.request({
//     host: 'pastebin.com',
//     port: 80,
//     path: '/api/api_post.php',
//     method: 'GET',
//     headers: {
//         // 'Content-Type': 'multipart/form-data',
//         'Content-Type': 'application/x-www-form-urlencoded',
//         'Content-Length': query.length
//     }
// }, function(res) {
//     var data = '';
//     res.on('data', function(chunk) {
//         data += chunk;
//     });
//     res.on('end', function() {
//         console.log(data);
//     });
// });
//
// req.write(query);
// req.end();


// var PastebinAPI = require('pastebin-js'),
//     pastebin = new PastebinAPI('devkey');
//     // pastebin = new PastebinAPI('2d84d80877824d333fa9fac2cf786bc7');
//
// var PastebinAPI = require('pastebin-js'),
//     pastebin = new PastebinAPI({
//         'api_dev_key' : '2d84d80877824d333fa9fac2cf786bc7',
//         'api_user_name' : 'PastebinUserName',
//         'api_user_password' : 'PastebinPassword'
//     });
//
// pastebin
//     .createPasteFromFile("./uploadtest.txt", "pastebin-js test", null, 1, "N")
//     .then(function (data) {
//         // we have succesfully pasted it. Data contains the id
//         console.log(data);
//     })
//     .fail(function (err) {
//         console.log(err);
//     });


module.exports = app;
