const Backbone = require('backbone');
const {eventsModel, eventsCollection} = require('./model-coll.js');
const {loginModel, loginCollection} = require('./model-login.js')
const {tokenModel, tokenCollection} = require('./model-gettoken.js')
const {userModel, userCollection} = require('./model-userInfo.js')
const STORE = require('./store.js');


const ACTIONS = {

   authenticateUser: function(userDataObj){
        let userMod = new UserModel()
        userMod.set(userDataObj)

        userMod.save().then(function(serverRes){
          location.hash="loginHome"
        })
  },


  fetchAuthToken: function(){

    let token = new tokenModel()

    token.fetch().then(function(){
      let wholeStrng = token.attributes.access_token
      let strngArry = wholeStrng.split('"')
      let theRealToken = strngArry[3].split('"')

      STORE.setStore("token", theRealToken)

    })
  },

  fetchUserData: function(){
    this.fetchAuthToken()

    STORE.onChange(function(){
      let theData = STORE.getStoreData()
      let myToken = theData.token[0]

      console.log(myToken)

      let theUserModel = new userCollection(myToken)

      theUserModel.fetch().then(function(){
        return theUserModel
      })
    })

  },

  fetchUserEventColl: function(){
    let events = new eventsCollection()

    events.fetch().then(function(){
      console.log(events.models[0].attributes)
      return events.models[0].attributes
    })
  },

  createNewmsg: function(){




 },


   handleUserLogin: function(usrInfo){
     let usrLogin = new loginModel()

      usrLogin.set(usrInfo)


      usrLogin.save().then(function(serverRes){


        //  console.log( "tickle me" ,serverRes)

        //  console.log(serverRes)


      })


   },


   changeView: function(viewInput){
      STORE.setStore('currentView', viewInput)
   }
}

module.exports = ACTIONS
