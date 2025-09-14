import { Routes, Route } from "react-router-dom"
import AdvancedSearch from "./components/pages/advancedSearch"
import DisciplinaryAnalysis from "./components/pages/disciplinaryAnalysis";
import SearchResult from "./components/pages/searchResult";
import PaperDetail from "./components/pages/paperDetail";

function App() {
    return (
        <>
            <Routes>
                <Route path="/" element={<AdvancedSearch />}></Route>
                <Route path="/disciplinaryAnalysis" element={<DisciplinaryAnalysis />}></Route>
                <Route path="/searchResult" element={<SearchResult />}></Route>
                {/* 新增：支持WOS_UID参数的详情页路由 */}
                <Route path="/detail/:wosUid" element={<PaperDetail />}></Route>
                {/* 保留兼容旧路由 */}
                <Route path="/detail" element={<PaperDetail />}></Route>
            </Routes>
        </>
    );
}

export default App;