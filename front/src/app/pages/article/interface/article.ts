import { Comment } from "../../comment/interface/comment.interface";
import { Theme } from "../../theme/interface/theme";

export interface Article {
    articleId: number;
    titre: string;
    contenu: string;
    auteur: string;
    themeId: number;
    user_id: number;
    createdAt?: Date;
    theme : Theme;
    commentaires : Comment [];
}
