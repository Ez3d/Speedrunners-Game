SPEEDRUNNERS
ICS 4U1 Final Project Game
Edward Zhou
Ms. Wong
2021-01-27

HOW TO PLAY:
    A - move left
    D - move right
    W - jump
    G - grappling hook

WHAT'S THE GOAL?
    You are a speedrunner/parkourist/runner/racer, and you want to complete a lap (or however many laps you like) 
    of a course as fast as you can. You have many ways to move and manuver, so make use of them!
    The timer starts when you are loaded into the map. Finish the race and record your time by touching the checkered flag.

THE CHARACTER CAN:
    - Double jump (jump an additional time while in the air)
    - Wall jump (jump an additional time when colliding with a wall, as if vaulting off it)
    - Shoot their grappling hook at a 45 degree angle in the direction they are facing
    - Show off the cool animations I spent too much time making

ADDITIONAL NOTES:
    - Speed boost pads don't launch the player--they increase max velocity for a limited time. The direction the point is purely
        aesthetic
    - Solid white surfaces are hookable surfaces. Dashed white walls are climable walls
    - Pressing H toggles hitboxes if you ever need to see them
    - There is no loss in energy when swinging from the grappling hook, so holding down G will let you swing indefinitely
        This isn't a bug. It was left out because it has little impact on the game 
        (in a race against time you don't need to hang from your hook)
    - Pressing ESCAPE pauses the game or is a substitute for back in the menus

CHANGES FROM PROPOSAL:
    - No distinct characters (and thus no character selection screen):
        I have never played the game this is based off of, so I originally thought different characters had different abilities,
        but the different characters are only there for aesthetics. Given the already abundant methods of movement 
        (grapple, wall jump, double jump, boost pads, etc.), more characters were not necessary

    - Limited powerups/special tiles
        The original game is naturally a multiplayer game, so many of the gadgets they use are tailored towards multiplayer
        (eg. freeze rays, homing rockets, stun bombs). I got excited and put a lot more than a single player racer needed on my
        proposal. The single-player friendly specials I did add were speed boosts and jump pads.

    - No mouse input for hooks:
        Hooks are controlled by keyboard, fired at a 45 degree angle in the direction the player is facing. Although I could have
        just as easily used mouse hooks, this would have made grappling hooks too easy to use and not rewarding.

    - Some parts took a lot longer to develop than I expected (even if I expected it to be difficult). These include:
        - Multiple methods for grappling hook physics
        - Multiple methods for collision detection (wonky because my movement includes acceleration)
        - Ramp movement (something I hadn't considered in my initial proposal)
        - Animation

    - I mentioned that I would add two-player if I had time. There was no time.

KNOWN BUGS/ISSUES:
    - When colliding with a ramp while grappling in a very specific way, an error occurs and the player is teleported to 
        the corner of the map. This is a very rare case and there wasn't a clear solution, so I left it.

    - At times, the player's hitbox does not line up well with the player model. This may make it seem like the player is
        partially in a wall, but it doesn't affect gameplay and is purely a visual thing. It also only happens rarely for very
        short frames (i.e. the peak of a grappling hook movement)