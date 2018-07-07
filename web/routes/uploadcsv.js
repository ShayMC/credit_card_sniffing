let express = require('express');
let router = express.Router();
const multer = require('multer');
const upload = multer({dest: 'upload/'});
const type = upload.single('recfile');
const fs = require('fs');
const path = require('path');

router.post('/post-file', type, function (req, res) {
    if (!req.file) {
        //
        uploadToHDFS("/Users/user/WebstormProjects/Big_Data_ex4/guns2.csv");
        //
        console.log("no file");
        res.status(400).json({message: 'no file'});
    }
    else {
        const path = req.file.path;
        if (!req.file.originalname.toLowerCase().match(/\.(csv)$/)) {
            fs.unlinkSync(path);
            res.status(400).json({message: 'wrong file'});
        }
        else {
            console.log(path);
            uploadToHDFS(path);
            fs.unlinkSync(path);
        }
    }
});

var hdfs = new (require("node-webhdfs")).WebHDFSClient({
    user: process.env.USER,
    namenode_host: "localhost",
    namenode_port: 50070
});
// var WebHDFS = require('webhdfs');
// var hdfs = WebHDFS.createClient();

function uploadToHDFS(hdfsFile) {
    console.log("here");
    var localFilePath = hdfsFile;
    var remoteFilePath = "/user/bigdata/guns.csv";

    var localFileStream = fs.createReadStream(localFilePath);
    var remoteFileStream = hdfs.createWriteStream(remoteFilePath);

    localFileStream.pipe(remoteFileStream);

    console.log("opening stream to HDFS");

    remoteFileStream.on('error', function onError(err) {
        // Do something with the error
        console.log("it failed");
        console.log(err);
    });

    remoteFileStream.on('finish', function onFinish() {
        // Upload is done
        console.log("it is done!");
    });
}

let firstline = require('firstline');

var load_data = require('../load_data');
router.get('/load-to-mongo', function (req, res) {


    // var fs = require('fs'),
    //     readline = require('readline');
    //
    // var rd = readline.createInterface({
    //     input: fs.createReadStream(path.resolve(req.query.name + ".csv")),
    //     output: process.stdout,
    //     console: false
    // });

    firstline(path.resolve(req.query.name + ".csv")).then(function (line) {
        if(line.charAt(line.length-1) === '\r'){
            line =line.substring(0,line.length-1)
        }
        let headers = line.split(",");
            var csvheaders = {
                REGIONS: {
                    headers: headers
                },
                STATES: {
                    headers: ['String']
                }
            };
            console.log("uploading " + req.query.name + ".csv");
            load_data.importFile(path.resolve(req.query.name + ".csv"), csvheaders);
            res.status(200).json({message:'success'});
    });
});

module.exports = router;