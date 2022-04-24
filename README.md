# SomNetworkCord
SwordofMagicNetwork傘下のサーバーチャットとDiscodチャットの連動SpigotPlugin

サーバー間チャットはSocketで通信しているのでBungeecord外とのServerともチャット連動可能

Bunggecord下で動いてる場合/hubでLobbyに戻れる(ServerIDがLobbyである必要有)

WebServerも入ってますがClient側が同梱されてないので実質使用不可

チャットのFormatとかはSomNetwork用に作ってるので固定

欲しい機能があったら @MomiNeko_ か MomiNeko#7759 にDMくれれば作るかもしれません



"ID"はサーバーの識別用IDとChatのPrefixを兼ねてます。

"Port"はサーバー間チャットの通信で使用するポートです。

"ResourcePack"にURLを設定できます。/resourcepackまたは/rpでリソースパックがロードされます

v2から汎用のString通信として使えるようになりました。
Server側はSom7専用機能以外はほぼルーターです

**使用方法 v1**

・サーバーが1つの場合[1]
  1. Configの"OperationMode"を"Server"にする。
  2. "DiscordBotToken"にDiscordBotのTokenを入れる。
  3. "DiscordChatChannel"に連動させたいテキストチャンネ。ルのIDを入れる。
 
・サーバーが2つ以上の場合[2]
  1. いずれかのサーバーで[1]の内容を設定する。
  2. [1]以外のすべてのサーバーの"OperationMode"を"Client"にする。
  3. "IP"に[1]で設定したサーバーのIPを入れる。(同PCの場合は"Localhost"でOK)

**使用方法 v2**
  1. SNC-Serverを起動する
  2. SomNetworkCord-Clientの鯖を起動する
  3. localhostならこれで終わり、外部ならconfig.ymlからHostを設定してください
  4. operationModeをNonChatにするとチャットシステムを無効化出来ます
  詳しいことは 猫宮紅葉 まで
