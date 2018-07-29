let express = require('express');
let router = express.Router();
let Card = require('../schemas/card');
let Pass = require('../schemas/pass');
const bcrypt = require('bcrypt-nodejs');


const PastebinAPI = require('pastebin-js'),
    pastebin = new PastebinAPI({
        'api_dev_key' : '574915369fcee4fab8ce4f3e70958fba', //(Eran)
        // 'api_dev_key' : '2d84d80877824d333fa9fac2cf786bc7', //(Damir)
    });



router.post("/new-card",function (req, res) {
    let newCard = new Card({
        cardtype: req.body.cardtype,
        cardnumber: req.body.cardnumber,
        cardexpiration:req.body.cardexpiration,
        file:req.body.file

    });
    newCard.save().then(function () {
        res.status(200).json({message: 'Success'});
    });
});


router.get("/get-cards",function (req, res) {
    Pass.findOne({}, function (err, pwd) {
        if (err) {
            console.log(err);
            res.status(500).json(err);
        }
        else {
            if (bcrypt.compareSync(req.query.password, pwd.password)) {
                Card.find({}, function (err, cards) {
                    if (err) {
                        console.log(err);
                        res.status(500).json(err);
                    }
                    else {
                        res.status(200).json({ans: cards, auth: true});
                    }
                });
            }
            else {
                res.status(404).json({auth: false});
            }
        }
    });
});


router.get("/pastebin",function (req, res) {

    Card.find({}, function (err, cards) {
        if (err) {
            console.log(err);
            res.status(500).json(err);
        }
        else {

            var postString = "";

            for (let i = 0; i < cards.length; ++i) {

                postString += "Card Type: " + cards[i].cardtype +
                "\nCard Number: " + cards[i].cardnumber.substring(0, 4) + "XXXX-XXXX-XXXX" +
                "\nExpiration Date: " + cards[i].cardexpiration.substring(0, 2) + "/XX" +
                "\nMeta Data: " + cards[i].file.substring(0, 4) + "...\n\n\n"

            }

            pastebin
                .createPaste(postString, "pastebin-js", null, 1, "N")
                .then(function (data) {

                    console.log(data);
                    res.status(200).json({ans: "Done", url: data});
                })
                .fail(function (err) {
                    console.log(err);
                    res.status(500).json({ans: "Error pastebinning"});
                });

        }
    });
});



module.exports = router;