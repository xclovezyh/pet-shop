import React from 'react';
import ReactDOM from 'react-dom/client';
import {
  Camera,
  Heart,
  Lock,
  LogIn,
  MapPin,
  MessageCircle,
  PawPrint,
  Plus,
  Search,
  ShieldCheck,
  Store,
  Tags,
  User
} from 'lucide-react';
import './styles.css';

const API_BASE = '/api';
const CONTACT_VALUE = '站内私信';
const phonePattern = /(?:\+?86[-\s]?)?1[3-9]\d{9}/;

type Category = {
  id: number;
  name: string;
  description: string;
  tags: string;
};

type Pet = {
  id: number;
  name: string;
  category: string;
  breed: string;
  age: string;
  city: string;
  status: string;
  price: number;
  imageUrl: string;
  healthInfo: string;
  personality: string;
};

type MarketPost = {
  id: number;
  title: string;
  type: string;
  category: string;
  city: string;
  description: string;
  author: string;
  contact: string;
  imageUrl: string;
};

type Moment = {
  id: number;
  author: string;
  petName: string;
  category?: string;
  content: string;
  likes: number;
  imageUrl: string;
};

type Region = {
  name: string;
  cities: Array<{
    name: string;
    districts: string[];
  }>;
};

const regions: Region[] = [
  {
    name: '上海市',
    cities: [{ name: '上海市', districts: ['浦东新区', '徐汇区', '静安区', '闵行区'] }]
  },
  {
    name: '浙江省',
    cities: [
      { name: '杭州市', districts: ['西湖区', '拱墅区', '滨江区', '余杭区'] },
      { name: '宁波市', districts: ['海曙区', '鄞州区', '江北区'] }
    ]
  },
  {
    name: '江苏省',
    cities: [
      { name: '南京市', districts: ['玄武区', '秦淮区', '建邺区'] },
      { name: '苏州市', districts: ['姑苏区', '吴中区', '工业园区'] }
    ]
  },
  {
    name: '广东省',
    cities: [
      { name: '广州市', districts: ['天河区', '越秀区', '番禺区'] },
      { name: '深圳市', districts: ['南山区', '福田区', '宝安区'] }
    ]
  }
];

const demoCategories: Category[] = [
  { id: 1, name: '猫咪', description: '温顺亲人，适合公寓和家庭陪伴。', tags: '新手友好,安静,陪伴型' },
  { id: 2, name: '狗狗', description: '活泼忠诚，需要规律运动和训练。', tags: '互动强,需要遛弯,家庭型' },
  { id: 3, name: '小宠', description: '仓鼠、兔子、龙猫等，占地小但需要细心照顾。', tags: '空间小,易观察,轻陪伴' },
  { id: 4, name: '水族', description: '观赏性强，适合打造安静的家居角落。', tags: '观赏型,低噪音,设备需求' }
];

const demoPets: Pet[] = [
  { id: 1, name: '团子', category: '猫咪', breed: '英短银渐层', age: '8个月', city: '上海市 浦东新区', status: '在售', price: 1800, imageUrl: '', healthInfo: '疫苗齐全，已驱虫', personality: '安静亲人，喜欢陪睡' },
  { id: 2, name: '可乐', category: '狗狗', breed: '柯基', age: '1岁', city: '浙江省 杭州市 西湖区', status: '可互换', price: 0, imageUrl: '', healthInfo: '体检正常，精力充沛', personality: '活泼黏人，会坐下握手' },
  { id: 3, name: '雪球', category: '小宠', breed: '侏儒兔', age: '5个月', city: '江苏省 南京市 玄武区', status: '可领养', price: 0, imageUrl: '', healthInfo: '健康，饮食稳定', personality: '胆小但熟悉后很亲近' }
];

const demoPosts: MarketPost[] = [
  { id: 1, title: '想给柯基找同城互换寄养伙伴', type: '互换', category: '狗狗', city: '浙江省 杭州市 西湖区', description: '工作日偶尔出差，希望找同城稳定互助家庭。', author: '林小满', contact: CONTACT_VALUE, imageUrl: '' },
  { id: 2, title: '英短银渐层找新家', type: '售卖', category: '猫咪', city: '上海市 上海市 浦东新区', description: '自家猫宝宝，疫苗驱虫记录完整，可预约看猫。', author: '阿舟', contact: CONTACT_VALUE, imageUrl: '' },
  { id: 3, title: '闲置猫爬架转让', type: '闲置', category: '猫咪', city: '江苏省 苏州市 姑苏区', description: '九成新，适合小户型，支持站内私信沟通。', author: '南栀', contact: CONTACT_VALUE, imageUrl: '' }
];

const demoMoments: Moment[] = [
  { id: 1, author: '林小满', petName: '团子', category: '猫咪', content: '今天第一次学会自己开零食罐，已经开始怀疑家里的安全系统。', likes: 28, imageUrl: '' },
  { id: 2, author: '阿舟', petName: '可乐', category: '狗狗', content: '雨停后去公园跑了两圈，回来直接睡成一张毯子。', likes: 16, imageUrl: '' }
];

function useApi<T>(path: string, fallback: T) {
  const [data, setData] = React.useState<T>(fallback);
  const [loading, setLoading] = React.useState(true);

  const load = React.useCallback(() => {
    setLoading(true);
    fetch(`${API_BASE}${path}`)
      .then((res) => res.json())
      .then(setData)
      .catch(() => setData(fallback))
      .finally(() => setLoading(false));
  }, [path, fallback]);

  React.useEffect(load, [load]);
  return { data, loading, reload: load };
}

function App() {
  const categories = useApi<Category[]>('/categories', demoCategories);
  const pets = useApi<Pet[]>('/pets', demoPets);
  const posts = useApi<MarketPost[]>('/posts', demoPosts);
  const moments = useApi<Moment[]>('/moments', demoMoments);
  const [currentUser, setCurrentUser] = React.useState(() => localStorage.getItem('petshop_user') || '');

  function login(name: string) {
    localStorage.setItem('petshop_user', name);
    setCurrentUser(name);
  }

  function logout() {
    localStorage.removeItem('petshop_user');
    setCurrentUser('');
  }

  const reloadFeeds = () => {
    posts.reload();
    moments.reload();
  };

  return (
    <main>
      <header className="topbar">
        <div className="brand">
          <PawPrint />
          <span>萌宠集市</span>
        </div>
        <nav>
          <a href="#categories">分类</a>
          <a href="#pets">展示</a>
          <a href="#market">售卖互换</a>
          <a href="#moments">日常</a>
        </nav>
        <LoginBox currentUser={currentUser} onLogin={login} onLogout={logout} />
      </header>

      <section className="hero">
        <div className="heroCopy">
          <p className="eyebrow">宠物百科 · 站内沟通 · 同城社区</p>
          <h1>把宠物展示、交易互换和日常分享放在一个清爽空间里</h1>
          <p className="lead">发布前需要登录；所有交易沟通仅支持站内私信，页面会拦截手机号，降低线下私联风险。</p>
          <div className="search">
            <Search size={20} />
            <input placeholder="搜索猫咪、柯基、互换、领养、闲置用品" />
          </div>
        </div>
        <div className="heroPanel">
          <Metric value={categories.data.length} label="分类库" />
          <Metric value={pets.data.length} label="展示宠物" />
          <Metric value={posts.data.length} label="交易帖子" />
        </div>
      </section>

      <section id="categories" className="section">
        <SectionTitle icon={<Tags />} title="宠物分类库" helper="发布内容时必须从这里选择分类" />
        <div className="categoryGrid">
          {categories.loading && <p>正在加载分类...</p>}
          {categories.data.map((category) => (
            <article className="category" key={category.id}>
              <h3>{category.name}</h3>
              <p>{category.description}</p>
              <div className="chips">
                {category.tags.split(',').map((tag) => (
                  <span key={tag}>{tag}</span>
                ))}
              </div>
            </article>
          ))}
        </div>
      </section>

      <section id="pets" className="section">
        <SectionTitle icon={<Store />} title="宠物展示与售卖" helper="先展示基本信息，后续可扩展订单和审核" />
        <div className="petGrid">
          {pets.data.map((pet) => (
            <article className="petCard" key={pet.id}>
              {imageBox(pet.imageUrl, pet.name)}
              <div className="petInfo">
                <div className="between">
                  <h3>{pet.name}</h3>
                  <span className="status">{pet.status}</span>
                </div>
                <p className="petBreed">{pet.breed} · {pet.age}</p>
                <p className="sub"><MapPin size={15} />{pet.city}</p>
                <p>{pet.personality}</p>
                <div className="between cardFooter">
                  <span className="price">{pet.price > 0 ? `￥${pet.price}` : '面议'}</span>
                  <span className="health"><ShieldCheck size={15} />{pet.healthInfo}</span>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section id="market" className="section split">
        <div>
          <SectionTitle icon={<Plus />} title="售卖 / 互换 / 领养帖子" helper="所有联系入口统一为站内私信" />
          <div className="postList">
            {posts.data.map((post) => (
              <article className="post" key={post.id}>
                <div className="between">
                  <span className="type">{post.type}</span>
                  <span className="postContact"><MessageCircle size={15} />{post.contact || CONTACT_VALUE}</span>
                </div>
                <h3>{post.title}</h3>
                <p>{post.description}</p>
                <div className="postMeta">
                  <span>{post.category}</span>
                  <span>{post.city}</span>
                  <span>{post.author}</span>
                </div>
              </article>
            ))}
          </div>
        </div>
        <Composer categories={categories.data} currentUser={currentUser} onSuccess={reloadFeeds} />
      </section>

      <section id="moments" className="section">
        <SectionTitle icon={<Camera />} title="用户日常分享" helper="记录宠物近况、养护经验和可爱的日常瞬间" />
        <div className="momentGrid">
          {moments.data.map((moment) => (
            <article className="moment" key={moment.id}>
              {imageBox(moment.imageUrl, moment.petName || moment.author)}
              <div>
                <span className="type">{moment.category || '日常'}</span>
                <h3>{moment.author} 和 {moment.petName}</h3>
                <p>{moment.content}</p>
                <span className="likes"><Heart size={15} />{moment.likes}</span>
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}

function LoginBox({ currentUser, onLogin, onLogout }: { currentUser: string; onLogin: (name: string) => void; onLogout: () => void }) {
  const [name, setName] = React.useState('');

  if (currentUser) {
    return (
      <div className="userBadge">
        <User size={16} />
        <span>{currentUser}</span>
        <button type="button" onClick={onLogout}>退出</button>
      </div>
    );
  }

  return (
    <form className="loginBox" onSubmit={(event) => {
      event.preventDefault();
      if (name.trim()) {
        onLogin(name.trim());
        setName('');
      }
    }}>
      <LogIn size={16} />
      <input value={name} onChange={(event) => setName(event.target.value)} placeholder="昵称登录" />
      <button type="submit">登录</button>
    </form>
  );
}

function Composer({ categories, currentUser, onSuccess }: { categories: Category[]; currentUser: string; onSuccess: () => void }) {
  const [mode, setMode] = React.useState<'post' | 'moment'>('post');
  const [imageUrl, setImageUrl] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState('');
  const [province, setProvince] = React.useState(regions[0].name);
  const selectedProvince = regions.find((item) => item.name === province) || regions[0];
  const [city, setCity] = React.useState(selectedProvince.cities[0].name);
  const selectedCity = selectedProvince.cities.find((item) => item.name === city) || selectedProvince.cities[0];
  const [district, setDistrict] = React.useState(selectedCity.districts[0]);

  React.useEffect(() => {
    const nextCity = selectedProvince.cities[0].name;
    setCity(nextCity);
    setDistrict(selectedProvince.cities[0].districts[0]);
  }, [province]);

  React.useEffect(() => {
    setDistrict(selectedCity.districts[0]);
  }, [city]);

  async function upload(file: File) {
    setError('');
    const form = new FormData();
    form.append('file', file);
    try {
      const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: form });
      if (!res.ok) {
        throw new Error(await readError(res));
      }
      const data = await res.json();
      setImageUrl(data.url);
    } catch (err) {
      setError(err instanceof Error ? err.message : '图片上传失败，请稍后重试。');
    }
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const formElement = event.currentTarget;
    if (!currentUser) {
      setError('请先登录后再发布内容。');
      return;
    }

    const form = new FormData(formElement);
    const body = Object.fromEntries(form.entries());
    const contentToCheck = Object.values(body).join(' ');
    if (phonePattern.test(contentToCheck)) {
      setError('请不要填写手机号，平台只允许站内私信沟通。');
      return;
    }

    setBusy(true);
    body.author = currentUser;
    body.city = `${province} ${city} ${district}`;
    body.contact = CONTACT_VALUE;
    if (imageUrl) {
      body.imageUrl = imageUrl;
    }

    try {
      const res = await fetch(`${API_BASE}/${mode === 'post' ? 'posts' : 'moments'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) {
        throw new Error(await readError(res));
      }
      formElement.reset();
      setImageUrl('');
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : '发布失败，请检查登录状态、分类、地区和内容是否符合规则。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <aside className="composer">
      <div className="composerHeader">
        <h3>发布中心</h3>
        <span>{currentUser ? `当前用户：${currentUser}` : '登录后可发布'}</span>
      </div>
      <div className="tabs">
        <button type="button" className={mode === 'post' ? 'active' : ''} onClick={() => setMode('post')}>交易帖</button>
        <button type="button" className={mode === 'moment' ? 'active' : ''} onClick={() => setMode('moment')}>日常</button>
      </div>
      {!currentUser && (
        <div className="locked">
          <Lock size={18} />
          <span>请先在右上角登录，之后才能发布帖子或日常。</span>
        </div>
      )}
      <form onSubmit={submit} className={!currentUser ? 'disabledForm' : ''}>
        {mode === 'post' ? (
          <>
            <input name="title" placeholder="标题" required disabled={!currentUser} />
            <select name="type" defaultValue="互换" disabled={!currentUser}>
              <option>互换</option>
              <option>售卖</option>
              <option>领养</option>
              <option>闲置</option>
              <option>求助</option>
            </select>
          </>
        ) : (
          <>
            <input name="petName" placeholder="宠物名字" required disabled={!currentUser} />
          </>
        )}
        <select name="category" required disabled={!currentUser}>
          {categories.map((category) => (
            <option key={category.id} value={category.name}>{category.name}</option>
          ))}
        </select>
        <RegionPicker
          province={province}
          city={city}
          district={district}
          selectedProvince={selectedProvince}
          selectedCity={selectedCity}
          disabled={!currentUser}
          onProvince={setProvince}
          onCity={setCity}
          onDistrict={setDistrict}
        />
        {mode === 'post' ? (
          <textarea name="description" placeholder="描述需求、宠物状态或交易条件。请勿填写手机号。" required disabled={!currentUser} />
        ) : (
          <textarea name="content" placeholder="分享今天的宠物日常。请勿填写手机号。" required disabled={!currentUser} />
        )}
        <div className="contactOnly">
          <MessageCircle size={16} />
          <span>联系方式固定为站内私信</span>
        </div>
        <label className="fileInput">
          <Camera size={18} />
          <span>{imageUrl ? '图片已上传' : '上传图片'}</span>
          <input type="file" accept="image/*" disabled={!currentUser} onChange={(e) => e.target.files?.[0] && upload(e.target.files[0])} />
        </label>
        {error && <p className="formError">{error}</p>}
        <button className="submit" disabled={busy || !currentUser}>{busy ? '发布中...' : '发布'}</button>
      </form>
    </aside>
  );
}

async function readError(res: Response) {
  try {
    const data = await res.json();
    return data.message || data.error || '发布失败，请检查填写内容。';
  } catch {
    return '发布失败，请检查后端服务是否正常运行。';
  }
}

function RegionPicker(props: {
  province: string;
  city: string;
  district: string;
  selectedProvince: Region;
  selectedCity: Region['cities'][number];
  disabled: boolean;
  onProvince: (value: string) => void;
  onCity: (value: string) => void;
  onDistrict: (value: string) => void;
}) {
  return (
    <div className="regionGrid">
      <select value={props.province} disabled={props.disabled} onChange={(event) => props.onProvince(event.target.value)}>
        {regions.map((item) => <option key={item.name}>{item.name}</option>)}
      </select>
      <select value={props.city} disabled={props.disabled} onChange={(event) => props.onCity(event.target.value)}>
        {props.selectedProvince.cities.map((item) => <option key={item.name}>{item.name}</option>)}
      </select>
      <select value={props.district} disabled={props.disabled} onChange={(event) => props.onDistrict(event.target.value)}>
        {props.selectedCity.districts.map((item) => <option key={item}>{item}</option>)}
      </select>
    </div>
  );
}

function Metric({ value, label }: { value: number; label: string }) {
  return (
    <div className="metric">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  );
}

function SectionTitle({ icon, title, helper }: { icon: React.ReactNode; title: string; helper: string }) {
  return (
    <div className="sectionTitle">
      <div>{icon}</div>
      <div>
        <h2>{title}</h2>
        <p>{helper}</p>
      </div>
    </div>
  );
}

function placeholder(label: string) {
  return (
    <div className="placeholder">
      <PawPrint size={34} />
      <span>{label}</span>
    </div>
  );
}

function imageBox(url: string, label: string) {
  if (!url) {
    return placeholder(label);
  }
  return <img className="cover" src={url} alt={label} />;
}

ReactDOM.createRoot(document.getElementById('root')!).render(<App />);
