(ns httpc.fortunes
  (:use clojure.java.io))

(def bender-quotes
     ["Everyone's a jerk. You, me, this jerk."
      "Ah well, there's always hope. A-HAHAHAHAHAAAH!!"
      "Okay kids, it's nine o'clock and you know what that means; daddy's sick of looking at you!"
      "That's me baby let's ditch the kids in the ally and have some fun"
      "Who are you, and why should I care?"
      "Bite my colossal metal ass."
      "This is the worst kind of discrimination. The kind against me!"
      "Oh. Your. God."
      "My life, and by extension everyone else's is meaningless."
      "I'm Bender, baby, please insert liquor!"
      "They're not very heavy, but you don't hear me not complaning."
      "I hate the people that love me and they hate me."
      "Do I preach to you while you're lying stoned in the gutter? No."
      "Tempers are wearing thin. Let's hope some robot doesn't kill everybody."
      "I'm gonna go build my own theme park! With blackjack and hookers! In fact, forget the park!"
      "That's no alien spaceship-that's my ass!"
      "Bribe is such an ugly word. I prefer extortion. The X makes it sound cool."
      "Life can be hilariously cruel."
      "Let's face it, comedy's a dead art form. Tragedy, now that's funny."
      "What kind of party is this? There's no booze and only one hooker."
      "I was thinking Benderbrau if it's an ale, Botweiser if it's a lager."
      "Ahhh, what an awful dream. Ones and zeroes everywhere... and I thought I saw a two."
      "Bite my 8-bit metal ass."
      "Oh wow, I can't believe how stupid I used to be and you still are."
      "Woohoo, time to go clubbin'! Baby seals here I come!"
      "In order to fix your leaky roof I'll need to spend two or three hours down here in the wine cellar."
      "I have everything I ever wanted: money, wealth, riches..."
      "Aww! Its anus looks like an asterisk."
      "Yes! I'm gonna be rich. You too but it's hard to get excited about that."
      "It's Elzar again! Oh, my God, I'm so excited, I wish I could wet my pants!"
      "You know the secret of traditional robot cooking? Start with a good, high-quality oil... then eat it."
      "Fry! Oh, I just knew you were still alive!... I owe you 10 bucks, Hermes!"
      "In the event of an emergency my ass can be used as a flotation device."
      "Guys, I swear those are prescription. I need 'em for reading stuff... on the other side of stuff."
      "Game's over losers! I have all the money. Compare your lives to mine and then kill yourselves!"
      "Gimmie you're biggest, strongest, cheapest drink."])

(defn next-bender-fortune []
  (rand-nth bender-quotes))
