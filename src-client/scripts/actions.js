const Backbone = require('backbone');
const {eventsModel, eventsCollection} = require('./model-coll.js');
const {loginModel, loginCollection} = require('./model-login.js')
const {tokenModel, tokenCollection} = require('./model-gettoken.js')
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
      console.log(token)
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
