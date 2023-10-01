
![logo](https://raw.githubusercontent.com/justADeni/StandAPI/master/src/img/logo.png)

# Simple yet powerful API for easy operation with fake ArmorStands

### Kotlin? But I use Java? 😢
Don't worry, you can use this library from either Kotlin or Java

### How do I use this thing? 😯
import StandAPI into your project and create an instance of PacketStand like so:
```Kotlin
//kotlin
val packetStand = PacketStand(location)
```
```Java
//java
PacketStand packetStand = new PacketStand(location)
```
then you can alter or get properties, for example
```kotlin
packetStand.setInvisible(true)
packetStand.isInvisible()

packetStand.setGlowingEffect(false)
packetStand.hasGlowingEffect()

packetStand.setCustomName("Bob")
packetStand.getCustomName()
```
or you can attach the PacketStand to an entity
*__note 1__: if a stand is attached to a Player, that player will not see the stand moving smoothly (all others will, however), but rather with small delay, more or less said player's ping. It is recommended to hide the stand for the player using .excludePlayer(player)*
*__note 2__: if the entity dies, stand will detach, unless the entity is a player*
```kotlin
//this will directly copy location of entity
packetStand.attachTo(entity)

//PacketStand will copy every move but will be two blocks above it's location
val offset = Offset(0.0,2.0,0.0)
packetStand.attachTo(entity)
```
this also copies the entity's head pitch and yaw movements by default
however, it can be disabled
```kotlin
packetStand.setAttachPitch(false)
packetStand.setAttachYaw(false)
```
stand can be made hidden for any number of players
```kotlin
packetStand.excludePlayer(player)
```
of course it can be moved
```kotlin
packetStand.setLocation(location)
```
head, body and each limb's rotation can be gotten or changed
StandAPI operates in regular degrees, 0-360, so no EulerAngles or any of that stuff
```kotlin
val pitch = packetStand.getHeadRotation().pitch
val yaw = packetStand.getHeadRotation().yaw
```
to change any rotation, create and pass [Rotation object](https://docshoster.org/p/justadeni/standapi/latest/com/github/justadeni/standapi/datatype/Rotation.html) like this
```kotlin
val rotation = Rotation(0f, 90f, 0f)
packetStand.setHeadRotation(rotation)
```
for full view of all methods, visit javadoc: <a href='https://docshoster.org/p/justadeni/standapi/latest/com/github/justadeni/standapi/PacketStand.html'>
  <img src='https://docshoster.org/pstatic/justadeni/standapi/latest/badge.svg'/>
</a>

 ## Features
- they have all functionality of regular armorstands
- they don't tick or cause lag, they aren't real entities
- they can be attached to any entity, including head pitch and yaw (can be customized)
- they can be hidden for certain players
- StandAPI also has a custom event for when player hits a PacketStand
- basic configuration
- saving and loading are built-in (but can be disabled)
- serializable and deserializable in one line of code
