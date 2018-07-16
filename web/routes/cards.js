let express = require('express');
let router = express.Router();
let Card = require('../schemas/card');
let Pass = require('../schemas/pass');
const bcrypt = require('bcrypt-nodejs');


const PastebinAPI = require('pastebin-js'),
    pastebin = new PastebinAPI({
        'api_dev_key' : '2d84d80877824d333fa9fac2cf786bc7',
        // 'api_user_name' : 'PastebinUserName',
        // 'api_user_password' : 'PastebinPassword'
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

            for (let i = 0; i < cards.length; ++i) {

                pastebin
                    .createPaste("Card Type: " + cards[i].cardtype +
                        "\nCard Number: " + cards[i].cardnumber.substring(0, 4) + "XXXX-XXXX-XXXX" +
                        "\nExpiration Date: " + cards[i].cardexpiration.substring(0, 2) + "/XX" +
                        "\nMeta Data: " + cards[i].file.substring(0, 2) + "...", "pastebin-js", null, 1, "N")
                    .then(function (data) {
                        // we have succesfully pasted it. Data contains the id
                        console.log(data);
                    })
                    .fail(function (err) {
                        console.log(err);
                    });


            }

            res.status(200).json({ans: "Done"});
        }
    });
});



module.exports = router;