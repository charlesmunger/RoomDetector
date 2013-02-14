RoomDetector
============

MOTIVATION
------------

Far too often phones ring when they shouldn't - In a meeting, movie theater, or other do-not-disturb setting. It would be a favor to everyone if phones could sense when they should silence their ringers - without the need of the user remembering to do so.

CONCEPT
-------

In our experimentation and survey of similar technologies in the field, we found some ways of potentially achieving room presence, and an understanding of why these simply don't work to the degree needed. Consumer GPS does not have the precision to differentiate between users located on either sides of walls; and suffers greatly indoors. WiFi signal strength is a decent indicator of proximity to an access point, but traverses walls well enough to suffer from the same pitfall as GPS. This lead us to think: what signal travels poorly through walls, and can be contained in a room? Sound - especially high frequency sound - is almost entirely contained behind walls and even windows at reasonable volume levels. And, luckily, there is a narrow band of sound (18Khz-20Khz) that though inaudible by most humans, is easily picked up by standard phone microphones and emit-able by practically any small speaker. Our project concept involves making use of this narrow band to transmit a 'quiet-time' signal in the room, and have any un-silenced phone check for the signal instead of immediately ringing - potentially vibrating or altering the way it notifies the user if it in fact realizes it should not disturb the room in quiet-time.

DETECTING ROOM PRESENCE
-----------------------

Our concept requires two components to successfully broadcast and detect a room presence signal - an emitter and a receiver (though these could be the very same device!). The emitter can be any device in the room capable of playing a high frequency sound: a computer, another phone, even a stereo music player with prerecorded signal on CD/USB drive. Any phone (and potentially other microphone-enabled devices) can serve as a receiver.

When room presence detection is desired in a room (e.g. when a meeting is in session in the conference room), an emitter will begin to play a high frequency sound pulse on a short interval (~0.5 seconds). Then, upon specified events - like an incoming phone call - receivers will divert the incoming call to run the preloaded software to detect if the device is within a room with an active emitter. By simply listening for slightly longer than the emitter interval (~0.75 seconds), the recorded sound clip should exhibit significant level fluctuation in that high frequency range if the phone is within listening range of the emitter; since it will hear the pulse sound level, and a baseline level when the pulse is not playing.

OTHER USES
----------

If and when we are able to successfully detect room presence, we realize there are many potential uses of this information other than silencing the phone upon a call. Some areas we will explore are:

— Media player auto-pause on call (only when phone in room)

— Locality plugin : offer the knowledge of room presence as a plugin to the popular location and sensory aware app

PROGRESS
--------

Feb 3 - Tested phone microphone frequency range. Using a spectral sound analyzer app, we found that a phone microphone was able to easily detect a signal at 18Khz. The signal was undetectable behind walls, yet completely detectable at any point in the room. It was also inaudible by us or anyone in the classroom.

Feb 4 - Tested the ability to intercept an incoming phone call, and run our test program instead. Successful.
