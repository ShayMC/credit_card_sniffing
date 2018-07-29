var less = require('less');
var fs = require('fs');
var path = require('path');

var lessCompiler = function () {

    var buffer = "";

    var files = fs.readdirSync(path.join(__dirname, "../public/stylesheets/less"));

    files.forEach(function (file, index) {
        buffer += fs.readFileSync(path.join(__dirname, "../public/stylesheets/less/") + file);
    });

    fs.writeFile(path.join(__dirname, "../public/stylesheets/main.less"), buffer, function (err) {
        if (err) {
            return console.log("Error writing css! " + err);
        }
    });




    fs.readFile('./public/stylesheets/main.less', function (err, data) {
        if (err) {
            return console.log("Error reading main.less. " + err);
        }
        data = data.toString();
        less.render(buffer)
            .then(function(output) {
                    fs.writeFileSync('./public/stylesheets/css/main.css', output.css);
                },
                function(err){
                    console.log("And error occurred during rendering less files. " + err);
                });

    });
};

var wizeupMinify = function() {
    var input = path.join(__dirname, "../public/stylesheets/css/main.css");

    minifier.on('error', function(err) {
        console.log("Error minifying CSS!");
    });
    minifier.minify(input, null);

};

var removers = function() {
    fs.unlinkSync(path.join(__dirname, "../public/stylesheets/main.less"));
    fs.unlinkSync(path.join(__dirname, "../public/stylesheets/css/main.css"));
};

module.exports = lessCompiler;