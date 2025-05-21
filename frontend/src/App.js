import { Routes, Route } from "react-router-dom"
import AdvancedSearch from "./components/pages/advancedSearch"
import GeneralSearch from "./components/pages/generalSearch";
import DisciplinaryAnalysis from "./components/pages/disciplinaryAnalysis";
import SearchResult from "./components/pages/searchResult";
import PaperDetail from "./components/pages/paperDetail";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<GeneralSearch />}></Route>
        <Route path="/advancedSearch" element={<AdvancedSearch />}></Route>
        <Route path="/disciplinaryAnalysis" element={<DisciplinaryAnalysis />}></Route>
        <Route path="/searchResult" element={<SearchResult />}></Route>
        <Route path="/detail" element={<PaperDetail />}></Route>
      </Routes>
    </>
  );
}

export default App;
