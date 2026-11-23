# WebSocket 与聊天模块逐行讲解

本文根据当前后端源码，解释 `config/WebSocketConfig.java` 以及 `chat/` 目录中的全部 Java 文件。代码行的讲解按源码顺序逐项对应；空行仅用于排版，不另作解释。本文涵盖 STOMP 连接认证、好友私聊、群聊、消息入库和历史分页。

源码根目录：`src/main/java/com/zhongxilin/ChatFlow/`

## 1. 先看整体消息路径

```text
客户端连接 /gs-guide-websocket
  └─ STOMP CONNECT 携带 Authorization: Bearer <JWT>
       └─ ChatAuthChannelInterceptor 解析 JWT，设置 Principal

客户端发送 /app/chat.private.send 或 /app/chat.group.send
  └─ ChatStompController 从 Principal 得到发送者 ID
       └─ ChatService 校验权限、转义正文、保存数据库
            └─ 私聊：分别投递到双方 /user/{id}/queue/chat
            └─ 群聊：广播到 /topic/chat.groups.{groupId}

客户端订阅群主题时
  └─ 拦截器再次检查该用户是否为有效群成员

历史消息走 HTTP GET /chat/private/{friendId} 或 /chat/group/{groupId}
  └─ 从 CurrentHolder 取登录用户，再经 ChatService 检查关系/成员资格
       └─ Mapper 查最近 50 条；服务层反转为时间正序
```

公共聊天室说明：当前 `chat/` 目录内的实现没有公共聊天室发送或历史代码；这里的私聊和群聊是独立模块。WebSocket 配置开放了通用 `/topic` 代理前缀，但具体公共聊天室业务应查看项目其他控制器（例如 `PublicChatRoomController`）及其消息对象，不能把它误认为本目录中的群聊逻辑。

## 2. `config/WebSocketConfig.java`

文件：`src/main/java/com/zhongxilin/ChatFlow/config/WebSocketConfig.java`

| 源码 | 逐行说明 |
|---|---|
| `package com.zhongxilin.ChatFlow.config;` | 声明配置类所在的 Java 包。包名必须与目录结构一致。 |
| `import ...Configuration;` | 引入 Spring 的配置类标记。 |
| `import ...MessageBrokerRegistry;` | 引入消息代理注册器，用来配置目的地前缀和消息代理。 |
| `import ...ChannelRegistration;` | 引入入站消息通道注册器，用于挂接拦截器。 |
| `import ...EnableWebSocketMessageBroker;` | 引入启用 STOMP 消息代理支持的注解。 |
| `import ...StompEndpointRegistry;` | 引入 STOMP 握手端点注册器。 |
| `import ...WebSocketMessageBrokerConfigurer;` | 引入可自定义 WebSocket 消息代理的配置接口。 |
| `import ...ChatAuthChannelInterceptor;` | 引入本项目的 STOMP 认证和群订阅鉴权拦截器。 |
| `@Configuration` | 告诉 Spring：这是一个配置类，会在应用启动时读取。 |
| `@EnableWebSocketMessageBroker` | 启用基于 WebSocket 的消息代理功能，应用可以接收 STOMP 帧并向目的地投递消息。 |
| `public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {` | 声明配置类并实现配置接口，以覆写需要的配置点。 |
| `/** ... */` | 注释说明下面字段用途。 |
| `private final ChatAuthChannelInterceptor ...;` | 保存认证拦截器引用；`final` 表示构造后不再替换。 |
| 构造函数 | Spring 通过构造器注入拦截器。这样该配置类能把拦截器挂到客户端入站通道。 |
| `configureMessageBroker(...)` | 配置服务端消息路由。 |
| `config.enableSimpleBroker("/topic", "/queue");` | 启用 Spring 内存型简单代理。`/topic` 通常用于广播，`/queue` 用于点对点队列；它本身不负责数据库持久化。 |
| `config.setApplicationDestinationPrefixes("/app");` | 客户端发往 `/app/...` 的消息会交给应用中的 `@MessageMapping` 方法，而不是直接作为代理订阅消息处理。 |
| `config.setUserDestinationPrefix("/user");` | 声明用户目的地前缀。配合 `convertAndSendToUser`，将消息路由到指定 Principal 的专属队列。 |
| `configureClientInboundChannel(...)` | 配置客户端进入服务端的 STOMP 消息通道。 |
| `registration.interceptors(chatAuthChannelInterceptor);` | 所有客户端入站帧经过该拦截器；实现重点是 CONNECT 身份认证和群主题 SUBSCRIBE 权限验证。 |
| `registerStompEndpoints(...)` | 注册客户端最初建立 WebSocket/STOMP 连接时访问的端点。 |
| `registry.addEndpoint("/gs-guide-websocket")` | 注册握手 URL。前端连接地址应包含此路径。 |
| `.setAllowedOriginPatterns("*")` | 允许任意来源发起跨域 WebSocket 握手，便于开发环境前后端分端口运行。部署时应结合实际域名限制来源。 |

## 3. `chat/security/ChatAuthChannelInterceptor.java`

| 源码 | 逐行说明 |
|---|---|
| `package ...chat.security;` | 声明聊天安全包。 |
| 各 `import` | 引入群 Mapper、JWT 工具、JWT Claims、Spring 消息/通道/STOMP API、`Principal` 和集合类型。 |
| `@Component` | 将拦截器注册为 Spring Bean，供 `WebSocketConfig` 注入。 |
| `implements ChannelInterceptor` | 表示该类可在消息进入通道前后检查或修改消息。 |
| `private final GroupMapper groupMapper;` | 用于查询群成员资格，尤其是订阅群主题时的授权判断。 |
| 构造函数 | 注入 `GroupMapper`。 |
| `preSend(Message<?> message, MessageChannel channel)` | 每个入站消息到达处理器前执行。返回原消息代表放行；抛出异常则拒绝该帧。`channel` 参数在当前实现中没有直接使用。 |
| `StompHeaderAccessor accessor = ...` | 从 Spring 消息头中取得 STOMP 专用访问器，以便读取命令、目的地和原生头。 |
| `if (accessor == null) return message;` | 不是 STOMP 格式或没有对应访问器时，不做本拦截器的 STOMP 检查，原样放行。 |
| `if (StompCommand.CONNECT.equals(...))` | 检测连接初始化帧。客户端通常在这一步提交令牌。 |
| `getNativeHeader("Authorization")` | 读取 STOMP CONNECT 的原生 `Authorization` 头。前端需要发送 `Bearer <JWT>`。 |
| 缺失头或不以 `Bearer ` 开头 | 立即抛出参数异常，拒绝没有令牌或格式错误的连接。 |
| `JwtUtils.parseToken(values.get(0).substring(7))` | 去掉 `Bearer ` 前缀并解析 JWT。令牌无效或过期时由 JWT 工具的异常路径拒绝。 |
| `claims.get("id")` | 从令牌声明中读取用户 ID。令牌签发端必须把用户 ID 放在 `id` 声明里。 |
| `if (id == null) ...` | 令牌虽能解析但没有用户 ID 时拒绝连接。 |
| `accessor.setUser((Principal) () -> id.toString());` | 把用户 ID 包装为当前 STOMP 会话的 Principal。后续控制器可通过 `Principal` 取身份，用户目的地也依赖该身份路由。 |
| `if (StompCommand.SUBSCRIBE.equals(...))` | 检测客户端订阅目的地的帧。 |
| `String destination = accessor.getDestination();` | 取得客户端想订阅的主题或队列地址。 |
| `startsWith("/topic/chat.groups.")` | 只对群聊主题做额外订阅授权；其他目的地不在该分支检查。 |
| `Principal user = accessor.getUser();` | 取 CONNECT 阶段绑定到会话上的认证身份。 |
| `if (user == null) ...` | 订阅群主题前必须已有认证用户。 |
| `Long.parseLong(destination.substring(...))` | 去掉群主题固定前缀，将剩余部分解析为群 ID。 |
| `catch (NumberFormatException ...)` | 目的地后缀不是数字时，按无效群聊地址拒绝。 |
| `groupMapper.countActiveMemberByIds(groupId, ...)` | 查询当前用户是否为该群有效成员。 |
| `== 0` | 查询不到有效成员记录时拒绝订阅，防止非成员监听群消息。 |
| `return message;` | 认证或权限检查通过后，将消息交给后续 Spring 处理流程。 |

## 4. `chat/controller/ChatStompController.java`

| 源码 | 逐行说明 |
|---|---|
| `@Controller` | 注册为 Spring MVC/STOMP 控制器。这里处理的是 WebSocket 消息映射，不是普通 HTTP 响应控制器。 |
| `private final ChatService chatService;` | 保存聊天业务服务。控制器负责接入和取身份，业务校验及持久化交给服务层。 |
| 构造函数 | 注入 `ChatService`。 |
| `@MessageMapping("/chat.private.send")` | 将发往 `/app/chat.private.send` 的 STOMP 消息路由到下一个方法；`/app` 来自 WebSocket 配置。 |
| `sendPrivate(SendChatMessage request, Principal principal)` | Spring 把消息 JSON 转成请求 DTO，把当前会话用户身份注入 `principal`。 |
| `chatService.sendPrivate(userId(principal), request);` | 解析可信发送者 ID 并调用服务层。请求体里没有可覆盖发送者身份的字段。 |
| `@MessageMapping("/chat.group.send")` | 将 `/app/chat.group.send` 映射到群聊发送方法。 |
| `sendGroup(...)` | 接收群消息请求和当前用户身份。 |
| `chatService.sendGroup(...)` | 交给服务层验证群成员、保存并广播。 |
| `userId(Principal principal)` | 私有辅助方法，将认证主体转换成 long 类型用户 ID。 |
| `principal == null` | 没有已认证主体时拒绝发送。 |
| `Long.parseLong(principal.getName())` | 约定 Principal 名称就是数字用户 ID；格式异常时操作失败。 |

## 5. `chat/service/ChatService.java`

| 源码 | 逐行说明 |
|---|---|
| `@Service` | 注册业务服务 Bean。 |
| imports | 引入 DTO、实体、Mapper、STOMP 投递模板、事务、HTML 转义和集合工具。 |
| 四个 `final` 字段 | 分别负责消息数据访问、好友关系验证、群成员验证和实时消息投递。 |
| 构造函数 | 通过构造器注入四项依赖。 |
| `@Transactional`（`sendPrivate`） | 私聊保存过程运行在数据库事务中；若事务内数据库操作失败则回滚。STOMP 推送与数据库事务不是天然的分布式事务。 |
| `sendPrivate(senderId, request)` | 处理一条好友私聊。发送者 ID 来自认证 Principal，接收者 ID 来自请求。 |
| `Long friendId = request.getFriendId()` | 取得对方用户 ID。 |
| `requireContent(...)` | 先验证正文不为空且长度不超过 5000。 |
| `friendId == null || friendId == senderId` | 拒绝没有接收者或给自己发私聊的请求。 |
| 两次 `countActiveFriendshipByIds` | 分别检查 sender→friend 和 friend→sender 的有效关系。该实现要求双向关系都存在。 |
| 关系计数为 0 | 拒绝非有效好友之间的私聊。 |
| `persist(1, 0L, senderId, friendId, ...)` | 保存消息；类型 1 表示私聊，会话 ID 当前固定为 0，发送者和接收者字段标识双方。 |
| 两次 `convertAndSendToUser` | 将同一条已保存消息分别推送给发送者和接收者的 `/queue/chat`。发送者也会收到回显，因此前端要避免重复追加。 |
| `return saved` | 返回完整消息视图；STOMP 控制器本身不把该返回值作为 HTTP 响应。 |
| `@Transactional`（`sendGroup`） | 群消息入库操作使用事务。 |
| `sendGroup(senderId, request)` | 处理群聊发送。 |
| 读取 groupId、调用 `requireContent` | 取得目标群并先检查正文。 |
| 空群 ID 或成员计数为 0 | 群 ID 必须存在，且发送者必须是该群有效成员。 |
| `persist(2, groupId, senderId, null, ...)` | 保存类型为 2 的群消息；会话 ID 是群 ID，群消息不指定单一接收者。 |
| `convertAndSend("/topic/chat.groups." + groupId, saved)` | 向该群主题广播。已订阅该主题的成员都会收到。主题订阅授权由入站拦截器执行。 |
| `privateHistory(userId, friendId, beforeId)` | 读取当前用户和好友之间的私聊历史。 |
| 两次好友关系查询 | 再次检查双向有效好友关系，防止已解除关系的用户继续读取历史。 |
| `findPrivateHistory(...)` | Mapper 取最多 50 条、ID 小于游标的记录。 |
| `new ArrayList<>(...)` | 创建可修改列表，因为 Mapper 返回的列表可能不允许原地修改。 |
| `Collections.reverse(history)` | SQL 为了取最新消息按 ID 降序，反转后按旧到新返回，便于聊天界面从上到下显示。 |
| `groupHistory(...)` | 读取群聊历史。 |
| `countActiveMemberByIds(...) == 0` | 只允许当前有效群成员读取群历史。 |
| `findGroupHistory(...)` 和 `reverse` | 取群最近 50 条，再变成时间正序。 |
| `persist(...)` | 私有保存共用方法，避免私聊和群聊重复写入逻辑。 |
| `new ChatMessageEntity()` | 创建待插入的消息实体。 |
| 一组 `set...` | 逐个设置会话类型、会话 ID、发送者、接收者和正文。 |
| `HtmlUtils.htmlEscape(content.trim())` | 去除首尾空白并转义 HTML 特殊字符，降低消息被当作 HTML 标记解释的风险。前端仍应按文本渲染。 |
| `messageMapper.insert(message)` | 插入数据库。Mapper 配置会把自增 ID 回填到实体。 |
| `findById(message.getId())` | 再查询刚插入记录，并联查发送者昵称、头像及数据库时间，形成实时广播/HTTP 历史共用的视图对象。 |
| `requireContent(content)` | 统一正文规则。 |
| `content == null || content.isBlank()` | 拒绝 null、空串和纯空白消息。 |
| `content.length() > 5000` | 拒绝超过 5000 个 Java 字符的正文。 |
| 抛出 `IllegalArgumentException` | 将业务输入问题交给全局异常处理流程转换成错误响应或错误帧。 |

## 6. `chat/mapper/ChatMessageMapper.java`

| 源码 | 逐行说明 |
|---|---|
| `@Mapper` | 让 MyBatis 扫描并为接口生成数据库访问实现。 |
| `insert(ChatMessageEntity message)` | 定义消息插入操作。 |
| `@Insert(...)` | SQL 将会话类型、会话 ID、发送者、接收者和正文写入 `cf_chat_message`。创建时间依赖数据库默认值。 |
| `@Options(useGeneratedKeys = true, keyProperty = "id")` | 使用数据库自增主键，并回填至 `message.id`。 |
| `findById(id)` | 按消息主键读取刚保存的完整消息。 |
| `@Select(...)` | 查询消息字段并联接 `cf_user`，取发送者用户名和头像；SQL 别名映射到 DTO 属性名。 |
| `JOIN cf_user u ON u.id=m.sender_id` | 每条消息必须能关联到发送者用户记录才会出现在结果中。 |
| `@Param("id")` | 明确 SQL 中 `#{id}` 对应的参数名。 |
| `findPrivateHistory(userId, friendId, beforeId)` | 查询两位用户之间的私聊消息。 |
| 私聊 SQL 条件 | `conversation_type=1` 且发送者/接收者组合任一方向匹配双方。 |
| `(beforeId IS NULL OR m.id < beforeId)` | 未提供游标时从最新记录开始；提供游标时只取更早的消息。 |
| `ORDER BY m.id DESC LIMIT 50` | 先取最新 50 条并按新到旧排序。服务层随后反转。 |
| `findGroupHistory(groupId, beforeId)` | 查询某群的历史消息。 |
| `conversation_type=2 AND conversation_id=groupId` | 限定群聊类型和目标群。 |
| `ORDER BY m.id DESC LIMIT 50` | 与私聊一样，按消息 ID 倒序取一页最多 50 条。 |

## 7. `chat/entity/ChatMessageEntity.java`

| 源码 | 逐行说明 |
|---|---|
| `@Data` | Lombok 自动生成 getter、setter、`toString`、`equals` 和 `hashCode`。 |
| `@AllArgsConstructor` | 生成包含全部字段的构造方法。 |
| `@NoArgsConstructor` | 生成无参构造方法，便于 MyBatis 创建实体。 |
| `id` | 数据库自增消息主键，插入时回填。 |
| `conversationType` | 会话类型：1 私聊，2 群聊。 |
| `conversationId` | 群聊时为群 ID；私聊现固定为 0，查询依靠 sender/recipient 两端 ID。 |
| `senderId` | 消息发送者用户 ID。 |
| `recipientId` | 私聊接收者 ID；群聊为空。 |
| `content` | 经服务层清理和 HTML 转义后的正文。 |
| `createdAt` | 数据库记录创建时间。 |

## 8. `chat/dto/SendChatMessage.java`

| 源码 | 逐行说明 |
|---|---|
| `@Data`、构造器注解 | Lombok 生成常用访问方法及构造方法。 |
| `friendId` | 私聊接收方 ID；群聊时不使用。 |
| `groupId` | 群聊目标 ID；私聊时不使用。 |
| `content` | 客户端提交的消息正文。 |
| `@NotBlank` | Bean Validation 约束：正文不能为 null、空串或纯空白。 |
| `@Size(max = 5000)` | 声明最大长度为 5000。当前 STOMP 方法参数未标 `@Valid`，因此实际业务校验仍由 `ChatService.requireContent` 明确执行。 |

## 9. `chat/dto/ChatMessageView.java`

| 源码 | 逐行说明 |
|---|---|
| Lombok 注解 | 自动生成访问器和构造方法。 |
| `id` | 消息数据库 ID，也可用作向前翻页的 `beforeId` 游标。 |
| `conversationType` | 1 私聊，2 群聊。 |
| `conversationId` | 私聊当前为 0，群聊为群 ID。 |
| `senderId` | 发送者 ID。 |
| `senderName` | 从用户表联查出的发送者用户名。 |
| `senderAvatar` | 从用户表联查出的头像地址。 |
| `recipientId` | 私聊接收者；群聊为空。 |
| `content` | 消息正文。 |
| `createdAt` | 数据库创建时间。该 View 用于实时推送和历史查询响应。 |

## 10. `chat/controller/ChatHistoryController.java`

| 源码 | 逐行说明 |
|---|---|
| `@RestController` | 注册 HTTP REST 控制器，方法返回对象会序列化为 JSON。 |
| `ChatService` 字段和构造函数 | 注入聊天服务，由服务执行权限检查及查询。 |
| `@GetMapping("/chat/private/{friendId}")` | 注册私聊历史 GET 接口，friendId 是路径变量。 |
| `@PathVariable long friendId` | 将 URL 中的好友 ID 转为 long。 |
| `@RequestParam(required = false) Long beforeId` | 可选分页游标；不传时取最新一页，传入时向更早记录翻页。 |
| `CurrentHolder.getCurrentId()` | 从 HTTP 认证过滤器建立的当前请求上下文读取登录用户 ID。它不是由请求参数决定的。 |
| `currentId == null` | 没有登录身份时返回业务错误。 |
| `chatService.privateHistory(...)` | 校验好友关系并读取历史消息。 |
| `Result.success(messages)` | 用项目统一成功响应包装消息列表。 |
| `@GetMapping("/chat/group/{groupId}")` | 注册群历史查询接口。 |
| 群接口参数和逻辑 | 读取群 ID、可选游标和当前用户 ID；未登录返回错误，否则交给服务层检查成员资格并查询。 |

## 11. 三类聊天能力边界

| 能力 | 当前代码路径 | 权限检查 | 投递/读取 |
|---|---|---|---|
| 好友私聊 | `/app/chat.private.send` | 发送前和读历史时都检查双方双向有效好友关系 | 双方 `/user/{id}/queue/chat`；HTTP 私聊历史接口 |
| 群聊 | `/app/chat.group.send` | 发送前检查成员；订阅群主题时拦截器检查；读历史时也检查 | `/topic/chat.groups.{groupId}`；HTTP 群历史接口 |
| 公共聊天室 | 不在 `chat/` 目录实现 | 需查看公共聊天室对应控制器和配置 | 本文只说明聊天模块共享的 STOMP 基础设施，不推断公共聊天室的具体业务规则 |

### 阅读代码时值得注意

1. `@MessageMapping` 的逻辑目的地需要加配置前缀 `/app` 才是客户端发送地址；`/topic` 和 `/queue` 是订阅侧代理目的地。
2. WebSocket 的身份来自 CONNECT JWT，并绑定到 `Principal`；请求 DTO 不接受 senderId，避免客户端伪造发送者。
3. 私聊消息会推送给发送者和接收者各一份；前端若发送后本地立即插入消息，需要根据消息 ID 去重。
4. 历史查询按 ID 倒序取 50 条，再在服务层翻转，因此响应顺序是旧到新。下一页应使用当前最旧消息的 ID 作为 `beforeId`。
5. 群主题名称中的群 ID 由客户端选择，因此订阅授权校验非常关键；发送权限和历史读取权限又分别在服务层独立检查。
