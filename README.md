# CustomTicket

讓你在遊戲裡用箱子介面做出「門票」物品。玩家手持門票按右鍵，確認後就會執行你設定好的指令，並消耗一張門票。

適合用來做兌換券、傳送票、禮包券、活動入場券等。

支援版本：1.21.x、26.x

---

## 功能

- **用介面建立門票**：不用改設定檔，在遊戲裡點一點就能做好
- **任何物品都能當門票**：點一下背包裡的物品，就會用它的樣子當門票
- **自訂名稱與描述**：支援顏色代碼
- **使用前會先確認**：玩家右鍵後會跳出視窗，顯示你寫的提示詞，選「是」才會使用，選「否」什麼都不會發生
- **可以設定多條指令**：使用門票時依序執行
- **使用權限**（選用）：只有擁有指定權限的玩家能使用
- **冷卻時間**（選用）：限制同一位玩家多久才能再用一次同一種門票
- **發光效果**（選用）：讓門票看起來像附魔過一樣發光
- **修改後自動更新**：編輯門票後，玩家手上、箱子裡已經發出去的門票也會跟著換成新的樣子
- **全繁體中文介面**，所有訊息能在設定檔中修改

---

## 指令

`/ct`、`/cticket`、`/c-ticket` 三種寫法效果相同。

| 指令 | 說明 |
|---|---|
| `/ct` | 查看插件版本並檢查是否有新版本 |
| `/ct create <名稱>` | 建立新門票 |
| `/ct edit <名稱>` | 編輯門票 |
| `/ct del <名稱>` | 刪除門票（也可以用 `/ct delete`） |
| `/ct give <名稱> [玩家] [數量]` | 給予門票，不填玩家就是給自己，不填數量就是 1 張 |
| `/ct list` | 查看所有門票，點擊名稱可以快速填入給予指令 |
| `/ct reload` | 重新載入設定與門票 |
| `/ct help` | 顯示指令說明 |

門票名稱只能使用英文字母、數字、底線 `_` 和減號 `-`。這個名稱只用在指令裡，門票在遊戲中顯示的名字可以另外設定成中文。

---

## 權限

| 權限 | 說明 | 預設 |
|---|---|---|
| `customticket.admin` | 使用所有管理指令（建立、編輯、刪除、給予等） | 管理員（OP） |
| `customticket.use` | 使用門票 | 所有玩家 |

---

## 如何建立門票

1. 輸入 `/ct create vip`，會打開門票編輯介面
2. **左鍵點擊你背包裡的物品**，設定成門票物品（不會拿走你的物品）
3. 點擊介面上的按鈕設定以下項目，需要打字的會請你在聊天欄輸入：
   - **門票名稱**：門票在遊戲中顯示的名字
   - **門票描述**：物品下方的說明文字，用 `|` 分隔多行
   - **右鍵執行的指令**：可以新增多條，也能修改、刪除、調整順序
   - **提示詞**：玩家使用時，確認視窗上顯示的文字，用 `|` 分隔多行
   - **使用權限**、**冷卻時間**、**附魔光效**：不需要可以不設
4. 點「確定並儲存」就完成了
5. 用 `/ct give vip` 拿一張來試試看

小提醒：
- 介面裡所有操作都使用**滑鼠左鍵**
- 在聊天欄輸入時，打 `取消` 或 `cancel` 可以放棄這次輸入
- 編輯到一半直接關掉介面，這次的修改不會被儲存

---

## 指令的寫法

每條指令前面可以加上標籤，決定由誰來執行：

| 標籤 | 說明 |
|---|---|
| `[console]` | 由伺服器後台執行（不加標籤時預設就是這個） |
| `[player]` | 由玩家自己執行，會受玩家本身的權限限制 |
| `[op]` | 暫時給玩家管理員身分執行，執行完立刻取消 |
| `[message]` | 不是指令，而是傳一段訊息給玩家 |

指令中可以使用以下文字，執行時會自動換成對應的內容：

| 文字 | 會換成 |
|---|---|
| `%player%` | 玩家名稱 |
| `%uuid%` | 玩家的 UUID |
| `%world%` | 玩家所在的世界 |
| `%x%` `%y%` `%z%` | 玩家所在的座標 |

範例：

```
[console] give %player% diamond 5
[console] title %player% title "歡迎！"
[player] spawn
[message] &a兌換成功！
```

指令開頭的 `/` 加不加都可以。

---

## 設定檔

`plugins/CustomTicket/config.yml` 可以修改：

- 插件訊息開頭的文字（`prefix`）
- 所有提示訊息與介面標題
- 是否檢查新版本、是否提醒管理員
- 聊天欄輸入的等待時間

插件更新後，如果新版本多了設定項目，會自動加進你原本的設定檔，不會覆蓋你改過的內容。

門票資料存放在 `plugins/CustomTicket/data/<名稱>.yml`，每張門票一個檔案。

---

## 注意事項

- 冷卻時間在伺服器重新啟動後會重新計算
- 刪除門票後，玩家手上的舊門票會無法使用，右鍵時會顯示「此門票已失效」
- 同一張門票同一時間只能由一位管理員編輯

---

## 下載與回報

- GitHub：https://github.com/clre20/CustomTicket
- 作者：Clre20


# CustomTicket

Create "ticket" items in-game using a chest menu. When a player holds a ticket and right-clicks, a confirmation window appears. After the player confirms, the commands you set up are run and one ticket is used up.

Useful for redeem coupons, teleport tickets, gift vouchers, event entry passes, and more.

Supported versions: 1.21.x, 26.x

---

## Features

- **Create tickets with a menu**: no need to edit config files, just click through it in-game
- **Any item can be a ticket**: click an item in your inventory and the ticket will use its look
- **Custom name and description**: color codes supported
- **Confirmation before use**: after right-clicking, players see a window with your prompt text. Choosing "Yes" uses the ticket, choosing "No" does nothing
- **Multiple commands**: run in order when the ticket is used
- **Use permission** (optional): only players with a specific permission can use the ticket
- **Cooldown** (optional): limits how often the same player can use the same kind of ticket
- **Glow effect** (optional): makes the ticket shine like an enchanted item
- **Auto-update after editing**: after you edit a ticket, tickets already handed out (in player inventories or in chests) change to the new look as well
- **Traditional Chinese interface**, and every message can be changed in the config file

---

## Commands

`/ct`, `/cticket`, and `/c-ticket` all work the same way.

| Command | Description |
|---|---|
| `/ct` | Show the plugin version and check for a new version |
| `/ct create <name>` | Create a new ticket |
| `/ct edit <name>` | Edit a ticket |
| `/ct del <name>` | Delete a ticket (`/ct delete` also works) |
| `/ct give <name> [player] [amount]` | Give tickets. Leave out the player to give to yourself, leave out the amount to give 1 |
| `/ct list` | List all tickets. Click a name to quickly fill in the give command |
| `/ct reload` | Reload settings and tickets |
| `/ct help` | Show command help |

Ticket names can only use English letters, numbers, underscores `_`, and hyphens `-`. This name is only used in commands. The name shown in-game can be set separately, in any language.

---

## Permissions

| Permission | Description | Default |
|---|---|---|
| `customticket.admin` | Use all management commands (create, edit, delete, give, etc.) | Operators (OP) |
| `customticket.use` | Use tickets | All players |

---

## How to Create a Ticket

1. Type `/ct create vip` to open the ticket editor
2. **Left-click an item in your inventory** to set it as the ticket item (your item is not taken)
3. Click the buttons in the menu to set the following. When typing is needed, you'll be asked to type in chat:
   - **Ticket name**: the name shown in-game
   - **Ticket description**: text shown under the item, use `|` to separate lines
   - **Commands run on right-click**: add as many as you need, and edit, delete, or reorder them
   - **Prompt text**: text shown in the confirmation window when a player uses the ticket, use `|` to separate lines
   - **Use permission**, **cooldown**, **glow effect**: leave them unset if you don't need them
4. Click "Confirm and Save" and you're done
5. Use `/ct give vip` to get one and try it out

Tips:
- Every action in the menus uses the **left mouse button**
- While typing in chat, enter `取消` or `cancel` to give up this input
- If you close the menu halfway through editing, your changes are not saved

---

## Writing Commands

You can put a tag in front of each command to decide who runs it:

| Tag | Description |
|---|---|
| `[console]` | Run by the server console (this is the default if no tag is given) |
| `[player]` | Run by the player, limited by the player's own permissions |
| `[op]` | Run by the player with temporary operator status, removed right after |
| `[message]` | Not a command; sends a message to the player |

You can use the following text in commands. It is replaced automatically when the command runs:

| Text | Replaced with |
|---|---|
| `%player%` | Player name |
| `%uuid%` | Player UUID |
| `%world%` | The world the player is in |
| `%x%` `%y%` `%z%` | The player's coordinates |

Examples:

```
[console] give %player% diamond 5
[console] title %player% title "Welcome!"
[player] spawn
[message] &aRedeemed successfully!
```

The leading `/` in a command is optional.

---

## Config File

In `plugins/CustomTicket/config.yml` you can change:

- The text at the start of plugin messages (`prefix`)
- All messages and menu titles
- Whether to check for new versions, and whether to notify operators
- How long to wait for chat input

After a plugin update, any new settings are added to your existing config file automatically, without overwriting what you have changed.

Ticket data is stored in `plugins/CustomTicket/data/<name>.yml`, one file per ticket.

---

## Notes

- Cooldowns reset when the server restarts
- After a ticket is deleted, old tickets players still hold can no longer be used, and right-clicking shows "此門票已失效" (this ticket is no longer valid)
- Only one operator can edit the same ticket at a time

---

## Download & Feedback

- GitHub: https://github.com/clre20/CustomTicket
- Author: Clre20
