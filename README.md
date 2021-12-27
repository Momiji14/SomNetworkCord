# SomNetworkCord
SwordofMagicNetwork傘下のサーバーチャットとDiscodチャットの連動SpigotPlugin

サーバー間チャットはSocketで通信しているのでBungeecord外とのServerともチャット連動可能

Bunggecord下で動いてる場合/hubでLobbyに戻れる(ServerIDがLobbyである必要有)

WebServerも入ってますがClient側が同梱されてないので実質使用不可

チャットのFormatとかはSomNetwork用に作ってるので固定(要望あったらConfigで帰れるようにするかも)

**使用方法**

・サーバーが1つの場合[1]
  1. Configの[OperationMode]を[Server]にする。
  2. [DiscordBotToken]にDiscordBotのTokenを入れる。
  3. [DiscordChatChannel]に連動させたいテキストチャンネ。ルのIDを入れる。
 
・サーバーが2つ以上の場合[2]
  1. いずれかのサーバーで[1]の内容を設定する。
  2. [1]以外のすべてのサーバーの[OperationMode]を[Client]にする。
  3. [IP]に[1]で設定したサーバーのIPを入れる。(同PCの場合は[Localhost]でOK)
 
[ID]はサーバーの識別用IDとChatのPrefixを兼ねてます。
[Port]はサーバー通信で使用するポートです。
