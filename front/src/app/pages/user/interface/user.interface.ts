import { Article } from "../../article/interface/article";
import { Theme } from "../../theme/interface/theme";

export interface User {
    id: number;
    email: string;
    firstName: string;
    password: string;
    createdAt: Date;
    follow: boolean,
    themes : Theme[];
    articles : Article[];
}
