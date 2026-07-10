import { createRoot } from "react-dom/client";
import "./index.css";

import RootApp from "./RootApp";

const root = createRoot(document.getElementById("root")!);
root.render(<RootApp />);
